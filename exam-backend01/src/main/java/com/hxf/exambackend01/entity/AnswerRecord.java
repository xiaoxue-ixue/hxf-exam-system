package com.hxf.exambackend01.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("answer_record")
public class AnswerRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examId;
    private Long studentId;
    private Long questionId;
    /** 学生答案 */
    private String userAnswer;
    /** 该题得分 */
    private Integer score;
    /** 是否正确 1对 0错（客观题） */
    private Integer isCorrect;
    private LocalDateTime createdAt;
}
