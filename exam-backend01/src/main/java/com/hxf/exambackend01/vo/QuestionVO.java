package com.hxf.exambackend01.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目列表项：附带分类名、题型中文名、难度中文名、创建人名。
 */
@Data
public class QuestionVO {
    private Long id;
    /** 题型整数，给前端判断用 */
    private Integer type;
    /** 题型中文名，给界面显示用 */
    private String typeName;
    private Long categoryId;
    private String categoryName;
    /** 题干（数据库字段名为 content） */
    private String stem;
    private String options;
    private String answer;
    private String analysis;
    private Integer difficulty;
    private String difficultyName;
    private Integer score;
    private Long creatorId;
    private String creatorName;
    private LocalDateTime createdAt;
}