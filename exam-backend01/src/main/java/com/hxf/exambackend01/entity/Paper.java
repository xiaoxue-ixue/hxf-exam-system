package com.hxf.exambackend01.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("paper")
public class Paper {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 试卷名称 */
    private String name;
    /** 总分 */
    private Integer totalScore;
    /** 考试时长(分钟) */
    private Integer duration;
    private Long creatorId;
    private LocalDateTime createdAt;
    /** 逻辑删除 0未删 1已删 */
    @TableLogic
    private Integer deleted;
}
