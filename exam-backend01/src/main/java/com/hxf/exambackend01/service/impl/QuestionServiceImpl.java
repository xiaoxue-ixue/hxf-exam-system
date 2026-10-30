package com.hxf.exambackend01.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hxf.exambackend01.common.BusinessException;
import com.hxf.exambackend01.dto.QuestionQueryDTO;
import com.hxf.exambackend01.dto.QuestionSaveDTO;
import com.hxf.exambackend01.entity.Question;
import com.hxf.exambackend01.entity.QuestionCategory;
import com.hxf.exambackend01.entity.SysUser;
import com.hxf.exambackend01.mapper.QuestionCategoryMapper;
import com.hxf.exambackend01.mapper.QuestionMapper;
import com.hxf.exambackend01.mapper.SysUserMapper;
import com.hxf.exambackend01.service.QuestionService;
import com.hxf.exambackend01.vo.QuestionVO;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;
    private final QuestionCategoryMapper categoryMapper;
    private final SysUserMapper sysUserMapper;

    public QuestionServiceImpl(QuestionMapper questionMapper,
                               QuestionCategoryMapper categoryMapper,
                               SysUserMapper sysUserMapper) {
        this.questionMapper = questionMapper;
        this.categoryMapper = categoryMapper;
        this.sysUserMapper = sysUserMapper;
    }

    /** 题型字符串 -> 整数 */
    private static final Map<String, Integer> TYPE_STR_TO_INT = Map.of(
            "SINGLE", 1, "MULTI", 2, "JUDGE", 3, "FILL", 4, "SHORT", 5);
    /** 整数 -> 题型字符串（第 7 周 Excel 导入用） */
    private static final String[] TYPE_INT_TO_STR = {"", "SINGLE", "MULTI", "JUDGE", "FILL", "SHORT"};
    /** 下标 0 占位，让"下标 = 题型编号" */
    private static final String[] TYPE_NAMES = {"", "单选题", "多选题", "判断题", "填空题", "简答题"};

    @Override
    public Page<QuestionVO> page(QuestionQueryDTO query) {
        Page<Question> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        // 题型：前端传字符串，先转整数再拼条件；非法值忽略（退化为不筛选）
        if (StringUtils.hasText(query.getType()) && TYPE_STR_TO_INT.containsKey(query.getType())) {
            wrapper.eq(Question::getType, TYPE_STR_TO_INT.get(query.getType()));
        }
        wrapper.eq(query.getCategoryId() != null, Question::getCategoryId, query.getCategoryId())
                .eq(query.getDifficulty() != null, Question::getDifficulty, query.getDifficulty())
                .like(StringUtils.hasText(query.getKeyword()), Question::getContent, query.getKeyword())
                .orderByDesc(Question::getId);
        Page<Question> qPage = questionMapper.selectPage(page, wrapper);

        // 一次性把分类名、用户名做成 Map，toVO 里不再查库（避免 N+1）
        Map<Long, String> categoryNameMap = getCategoryNameMap();
        Map<Long, String> userNameMap = getUserNameMap();

        Page<QuestionVO> voPage = new Page<>(qPage.getCurrent(), qPage.getSize(), qPage.getTotal());
        voPage.setRecords(qPage.getRecords().stream()
                .map(q -> toVO(q, categoryNameMap, userNameMap))
                .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public QuestionVO detail(Long id) {
        Question q = questionMapper.selectById(id);
        if (q == null) {
            throw new BusinessException("题目不存在");
        }
        return toVO(q, getCategoryNameMap(), getUserNameMap());
    }

    @Override
    public void save(QuestionSaveDTO dto) {
        validate(dto);
        Question question = toEntity(dto);
        // 创建人由后端从 token 里取，不能前端传
        question.setCreatorId(getCurrentUserId());
        questionMapper.insert(question);
    }

    @Override
    public void update(QuestionSaveDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("题目 ID 不能为空");
        }
        validate(dto);
        Question question = toEntity(dto);
        // toEntity 没设 creatorId / createdAt，updateById 会跳过 null 字段，创建信息保持不变
        questionMapper.updateById(question);
    }

    @Override
    public void delete(Long id) {
        // @TableLogic 生效，实际执行 UPDATE ... SET deleted=1
        questionMapper.deleteById(id);
    }

    // ================= 私有工具方法 =================

    private QuestionVO toVO(Question q, Map<Long, String> categoryNameMap, Map<Long, String> userNameMap) {
        QuestionVO vo = new QuestionVO();
        vo.setId(q.getId());
        vo.setType(q.getType());
        if (q.getType() != null && q.getType() >= 1 && q.getType() <= 5) {
            vo.setTypeName(TYPE_NAMES[q.getType()]);
        }
        vo.setCategoryId(q.getCategoryId());
        vo.setCategoryName(categoryNameMap.getOrDefault(q.getCategoryId(), ""));
        vo.setStem(q.getContent());
        vo.setOptions(q.getOptions());
        vo.setAnswer(q.getAnswer());
        vo.setAnalysis(q.getAnalysis());
        vo.setDifficulty(q.getDifficulty());
        Map<Integer, String> diffMap = Map.of(1, "简单", 2, "中等", 3, "困难");
        vo.setDifficultyName(diffMap.getOrDefault(q.getDifficulty(), "未知"));
        vo.setScore(q.getScore());
        vo.setCreatorId(q.getCreatorId());
        vo.setCreatorName(userNameMap.getOrDefault(q.getCreatorId(), ""));
        vo.setCreatedAt(q.getCreatedAt());
        return vo;
    }

    private void validate(QuestionSaveDTO dto) {
        if (!StringUtils.hasText(dto.getStem())) {
            throw new BusinessException("题干不能为空");
        }
        if (!StringUtils.hasText(dto.getType()) || !TYPE_STR_TO_INT.containsKey(dto.getType())) {
            throw new BusinessException("题型不合法");
        }
        int typeInt = TYPE_STR_TO_INT.get(dto.getType());
        if (typeInt <= 2 && !StringUtils.hasText(dto.getOptions())) {
            throw new BusinessException("选择题必须填写选项");
        }
        if (!StringUtils.hasText(dto.getAnswer())) {
            throw new BusinessException("答案不能为空");
        }
    }

    /** 从 SecurityContext 取当前登录用户名，再查库拿 userId */
    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof String username) {
            SysUser user = sysUserMapper.selectOne(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
            return user != null ? user.getId() : null;
        }
        return null;
    }

    private Question toEntity(QuestionSaveDTO dto) {
        Question q = new Question();
        q.setId(dto.getId());
        // 字符串题型转整数；非法值兜底成单选（validate 已挡住非法值）
        if (dto.getType() != null && TYPE_STR_TO_INT.containsKey(dto.getType())) {
            q.setType(TYPE_STR_TO_INT.get(dto.getType()));
        } else {
            q.setType(1);
        }
        q.setCategoryId(dto.getCategoryId());
        // stem -> content
        q.setContent(dto.getStem());
        q.setOptions(dto.getOptions());
        q.setAnswer(dto.getAnswer());
        q.setAnalysis(dto.getAnalysis());
        q.setDifficulty(dto.getDifficulty() != null ? dto.getDifficulty() : 1);
        q.setScore(dto.getScore() != null ? dto.getScore() : 5);
        return q;
    }

    /** 分类 id -> 分类名 */
    private Map<Long, String> getCategoryNameMap() {
        return categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(QuestionCategory::getId, QuestionCategory::getName, (a, b) -> a));
    }

    /** 用户 id -> 显示名（优先真实姓名，无则用账号） */
    private Map<Long, String> getUserNameMap() {
        List<SysUser> users = sysUserMapper.selectList(null);
        return users.stream()
                .collect(Collectors.toMap(SysUser::getId,
                        u -> u.getRealName() != null && StringUtils.hasText(u.getRealName())
                                ? u.getRealName() : u.getUsername(),
                        (a, b) -> a));
    }
}