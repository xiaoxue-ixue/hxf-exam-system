package com.hxf.exambackend01.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hxf.exambackend01.dto.QuestionQueryDTO;
import com.hxf.exambackend01.dto.QuestionSaveDTO;
import com.hxf.exambackend01.vo.QuestionVO;

/**
 * 题目服务：分页查询、详情、增删改。
 */
public interface QuestionService {
    Page<QuestionVO> page(QuestionQueryDTO query);

    QuestionVO detail(Long id);

    void save(QuestionSaveDTO dto);

    void update(QuestionSaveDTO dto);

    void delete(Long id);
}