package com.hxf.exambackend01.service;

import com.hxf.exambackend01.entity.QuestionCategory;
import com.hxf.exambackend01.vo.CategoryVO;

import java.util.List;

/**
 * 题目分类服务。
 */
public interface QuestionCategoryService {
    /** 返回嵌套结构的分类树 */
    List<CategoryVO> getTree();

    void save(QuestionCategory category);

    void update(QuestionCategory category);

    void delete(Long id);
}