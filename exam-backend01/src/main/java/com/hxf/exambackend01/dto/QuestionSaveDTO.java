package com.hxf.exambackend01.dto;

import lombok.Data;

/**
 * 题目新增 / 编辑参数。
 *
 * <p>前端提交 type 为字符串枚举（SINGLE/MULTI/JUDGE/FILL/SHORT），
 * 后端转为整数存储；前端提交 stem 映射到数据库 content 字段。</p>
 */
@Data
public class QuestionSaveDTO {
    private Long id;
    /** 前端传字符串题型，如 "SINGLE"、"MULTI" */
    private String type;
    private Long categoryId;
    /** 前端字段名 stem，对应数据库 content */
    private String stem;
    /** 选择题选项 JSON 字符串 */
    private String options;
    private String answer;
    private String analysis;
    private Integer difficulty;
    private Integer score;
}