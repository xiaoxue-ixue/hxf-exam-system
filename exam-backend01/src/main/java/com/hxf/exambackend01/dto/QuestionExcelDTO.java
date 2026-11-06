package com.hxf.exambackend01.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 题目 Excel 行模型：用于批量导入 / 导出 / 生成模板。
 *
 * <p>每一列用 {@code @ExcelProperty} 标注表头名，EasyExcel 读写时
 * 会按表头名自动匹配对应字段（与列顺序无关，但表头名必须一字不差）。</p>
 */
@Data
public class QuestionExcelDTO {
    /** 题型（中文）：单选题/多选题/判断题/填空题/简答题 */
    @ExcelProperty("题型")
    private String typeName;
    /** 题干 */
    @ExcelProperty("题干")
    private String content;
    /** 分类名称（需在「题目分类」中已存在） */
    @ExcelProperty("分类")
    private String categoryName;
    /** 选项 A（选择题用，判断题/填空/简答留空） */
    @ExcelProperty("选项A")
    private String optionA;
    /** 选项 B */
    @ExcelProperty("选项B")
    private String optionB;
    /** 选项 C */
    @ExcelProperty("选项C")
    private String optionC;
    /** 选项 D */
    @ExcelProperty("选项D")
    private String optionD;
    /** 正确答案：单选 A-D、多选 A,C、判断 对/错、填空逗号分隔、简答文本 */
    @ExcelProperty("正确答案")
    private String answer;
    /** 难度（中文）：简单/中等/困难 */
    @ExcelProperty("难度")
    private String difficultyName;
    /** 分值 */
    @ExcelProperty("分值")
    private Integer score;
    /** 解析（选填） */
    @ExcelProperty("解析")
    private String analysis;
}