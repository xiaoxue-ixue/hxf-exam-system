package com.hxf.exambackend01.controller;

import com.hxf.exambackend01.common.R;
import com.hxf.exambackend01.entity.QuestionCategory;
import com.hxf.exambackend01.service.QuestionCategoryService;
import com.hxf.exambackend01.vo.CategoryVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题目分类管理接口。
 */
@RestController
@RequestMapping("/category")
public class QuestionCategoryController {

    private final QuestionCategoryService categoryService;

    public QuestionCategoryController(QuestionCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /** 分类树 */
    @GetMapping("/tree")
    public R<List<CategoryVO>> tree() {
        return R.success(categoryService.getTree());
    }

    /** 新增分类 */
    @PostMapping
    public R<Void> add(@RequestBody QuestionCategory category) {
        categoryService.save(category);
        return R.success();
    }

    /** 编辑分类 */
    @PutMapping
    public R<Void> update(@RequestBody QuestionCategory category) {
        categoryService.update(category);
        return R.success();
    }

    /** 删除分类（物理删除） */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return R.success();
    }
}