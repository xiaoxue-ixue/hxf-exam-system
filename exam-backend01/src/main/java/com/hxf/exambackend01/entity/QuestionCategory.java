package com.hxf.exambackend01.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("question_category")
public class QuestionCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 分类名称 */
    private String name;
    /** 父分类ID，0为顶级 */
    private Long parentId;
    /** 排序 */
    private Integer sort;
    private LocalDateTime createdAt;
}
