package com.hxf.exambackend01.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("question")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 题型 1单选 2多选 3判断 4填空 5简答 */
    private Integer type;
    private Long categoryId;
    /** 题干 */
    private String content;
    /** 选项（JSON，选择题用） */
    private String options;
    private String answer;
    /** 解析 */
    private String analysis;
    /** 难度 1简单 2中等 3困难 */
    private Integer difficulty;
    /** 默认分值 */
    private Integer score;
    private Long creatorId;
    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;
}
