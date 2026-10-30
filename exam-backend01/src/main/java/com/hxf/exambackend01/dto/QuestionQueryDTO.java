package com.hxf.exambackend01.dto;

import lombok.Data;

/**
 * 题目分页查询参数：支持按题型、分类、难度、题干关键字筛选。
 */
@Data
public class QuestionQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    /** 前端传字符串题型，如 "SINGLE" */
    private String type;
    private Long categoryId;
    private Integer difficulty;
    /** 前端传 keyword，映射到数据库 content 字段 */
    private String keyword;
}