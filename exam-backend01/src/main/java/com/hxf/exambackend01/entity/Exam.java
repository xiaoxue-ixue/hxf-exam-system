package com.hxf.exambackend01.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exam")
public class Exam {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long paperId;
    /** 考试名称 */
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    /** 时长(分钟) */
    private Integer duration;
    /** 及格分 */
    private Integer passScore;
    /** 状态 0未发布 1已发布 */
    private Integer status;
    private Long creatorId;
    private LocalDateTime createdAt;
}
