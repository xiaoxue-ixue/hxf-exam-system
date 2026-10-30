package com.hxf.exambackend01.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hxf.exambackend01.common.BusinessException;
import com.hxf.exambackend01.entity.Question;
import com.hxf.exambackend01.entity.QuestionCategory;
import com.hxf.exambackend01.mapper.QuestionCategoryMapper;
import com.hxf.exambackend01.mapper.QuestionMapper;
import com.hxf.exambackend01.service.QuestionCategoryService;
import com.hxf.exambackend01.vo.CategoryVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class QuestionCategoryServiceImpl implements QuestionCategoryService {

    private final QuestionCategoryMapper categoryMapper;
    private final QuestionMapper questionMapper;

    public QuestionCategoryServiceImpl(QuestionCategoryMapper categoryMapper, QuestionMapper questionMapper) {
        this.categoryMapper = categoryMapper;
        this.questionMapper = questionMapper;
    }

    @Override
    public List<CategoryVO> getTree() {
        List<QuestionCategory> list = categoryMapper.selectList(
                new LambdaQueryWrapper<QuestionCategory>().orderByAsc(QuestionCategory::getSort));
        // 顶级分类的 parent_id 是 0，这里必须传 0L，传 null 会返回空数组
        return buildTree(list, 0L);
    }

    private List<CategoryVO> buildTree(List<QuestionCategory> all, Long parentId) {
        return all.stream()
                .filter(c -> Objects.equals(c.getParentId(), parentId))
                .map(c -> {
                    CategoryVO vo = new CategoryVO();
                    vo.setId(c.getId());
                    vo.setName(c.getName());
                    vo.setParentId(c.getParentId());
                    vo.setSort(c.getSort());
                    vo.setChildren(buildTree(all, c.getId()));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void save(QuestionCategory category) {
        // 同一父分类下名称唯一
        Long count = categoryMapper.selectCount(
                new LambdaQueryWrapper<QuestionCategory>()
                        .eq(QuestionCategory::getParentId, category.getParentId())
                        .eq(QuestionCategory::getName, category.getName()));
        if (count > 0) {
            throw new BusinessException("同级分类名称已存在");
        }
        categoryMapper.insert(category);
    }

    @Override
    public void update(QuestionCategory category) {
        // 唯一性校验必须排除自己，否则只改 sort 也会报"名称已存在"
        Long count = categoryMapper.selectCount(
                new LambdaQueryWrapper<QuestionCategory>()
                        .eq(QuestionCategory::getParentId, category.getParentId())
                        .eq(QuestionCategory::getName, category.getName())
                        .ne(QuestionCategory::getId, category.getId()));
        if (count > 0) {
            throw new BusinessException("同级分类名称已存在");
        }
        // 兜底：前端已过滤「上级分类」下拉，但直接调接口仍可能把父节点指成自己的后代，形成环
        checkParent(category.getId(), category.getParentId());
        categoryMapper.updateById(category);
    }

    /**
     * 校验 parentId 不会让自己成环：不能选自己，也不能选自己的子孙。
     * 从目标父节点沿 parent_id 一路向上，若途中遇到自己则说明目标父节点在自己子树里。
     */
    private void checkParent(Long id, Long parentId) {
        if (parentId == null || parentId == 0) {
            return;
        }
        if (Objects.equals(id, parentId)) {
            throw new BusinessException("上级分类不能是自己");
        }
        Long cursor = parentId;
        for (int guard = 0; cursor != null && cursor != 0 && guard < 100; guard++) {
            if (Objects.equals(cursor, id)) {
                throw new BusinessException("上级分类不能是自己的子分类");
            }
            QuestionCategory parent = categoryMapper.selectById(cursor);
            if (parent == null) {
                break;
            }
            cursor = parent.getParentId();
        }
    }

    @Override
    public void delete(Long id) {
        // 物理删除前先挡两种会留下孤儿数据的情况
        Long childCount = categoryMapper.selectCount(
                new LambdaQueryWrapper<QuestionCategory>().eq(QuestionCategory::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("该分类下有子分类，请先删除子分类");
        }
        Long questionCount = questionMapper.selectCount(
                new LambdaQueryWrapper<Question>().eq(Question::getCategoryId, id));
        if (questionCount > 0) {
            throw new BusinessException("该分类下有题目，无法删除");
        }
        categoryMapper.deleteById(id);
    }
}