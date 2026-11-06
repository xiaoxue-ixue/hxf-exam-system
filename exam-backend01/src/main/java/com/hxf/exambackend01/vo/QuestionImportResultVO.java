package com.hxf.exambackend01.vo;

import lombok.Data;

import java.util.List;

/**
 * 题目批量导入结果。
 *
 * <p>逐行解析 Excel 后，合法的题目直接入库，非法的记录到 errors 中
 * 返回给前端展示，便于用户定位并修正后重新导入。</p>
 */
@Data
public class QuestionImportResultVO {
    /** 成功导入条数 */
    private Integer successCount;
    /** 失败条数 */
    private Integer failCount;
    /** 失败明细（行号 + 原因） */
    private List<ImportErrorVO> errors;
}