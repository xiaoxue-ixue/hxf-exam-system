package com.hxf.exambackend01.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hxf.exambackend01.common.R;
import com.hxf.exambackend01.dto.QuestionQueryDTO;
import com.hxf.exambackend01.dto.QuestionSaveDTO;
import com.hxf.exambackend01.service.QuestionService;
import com.hxf.exambackend01.vo.QuestionVO;
import org.springframework.web.bind.annotation.*;

/**
 * 题目管理接口。
 */
@RestController
@RequestMapping("/question")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /** 分页 + 4 个筛选条件 */
    @GetMapping("/page")
    public R<Page<QuestionVO>> page(QuestionQueryDTO query) {
        return R.success(questionService.page(query));
    }

    /** 详情；字面量路径 /page 优先于 /{id}，不会冲突 */
    @GetMapping("/{id}")
    public R<QuestionVO> detail(@PathVariable Long id) {
        return R.success(questionService.detail(id));
    }

    /** 新增 */
    @PostMapping
    public R<Void> add(@RequestBody QuestionSaveDTO dto) {
        questionService.save(dto);
        return R.success();
    }

    /** 编辑 */
    @PutMapping
    public R<Void> update(@RequestBody QuestionSaveDTO dto) {
        questionService.update(dto);
        return R.success();
    }

    /** 删除（逻辑删除） */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return R.success();
    }
}