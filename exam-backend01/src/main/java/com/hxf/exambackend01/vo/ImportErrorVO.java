package com.hxf.exambackend01.vo;

import lombok.Data;

/**
 * 导入单行错误信息。
 */
@Data
public class ImportErrorVO {
    /** Excel 中的行号（从 1 开始，含表头行） */
    private Integer row;
    /** 失败原因 */
    private String reason;

    public ImportErrorVO() {
    }

    /** 监听器里高频构造，省掉「new 完再 set 两次」 */
    public ImportErrorVO(Integer row, String reason) {
        this.row = row;
        this.reason = reason;
    }
}