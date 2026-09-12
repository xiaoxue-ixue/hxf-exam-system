package com.hxf.exambackend01.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exam_result")
public class ExamResult {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examId;
    private Long studentId;
    /** 客观题得分 */
    private Integer objectiveScore;
    /** 主观题得分 */
    private Integer subjectiveScore;
    /** 总分 */
    private Integer totalScore;
    /** 状态 0待阅卷 1已完成 */
    private Integer status;
    private LocalDateTime submitTime;
}
