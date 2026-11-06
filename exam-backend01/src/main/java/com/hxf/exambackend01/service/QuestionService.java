package com.hxf.exambackend01.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hxf.exambackend01.dto.QuestionQueryDTO;
import com.hxf.exambackend01.dto.QuestionSaveDTO;
import com.hxf.exambackend01.vo.QuestionImportResultVO;
import com.hxf.exambackend01.vo.QuestionVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;

/**
 * 题目服务：分页查询、详情、增删改，以及 Excel 批量导入 / 导出 / 模板下载。
 */
public interface QuestionService {
    Page<QuestionVO> page(QuestionQueryDTO query);

    QuestionVO detail(Long id);

    void save(QuestionSaveDTO dto);

    void update(QuestionSaveDTO dto);

    void delete(Long id);

    /** 批量导入：解析 Excel、逐行校验、合法数据入库并返回逐行结果 */
    QuestionImportResultVO importExcel(MultipartFile file);

    /** 导出：按筛选条件查询题目并写入 Excel 输出流 */
    void export(QuestionQueryDTO query, OutputStream outputStream);

    /** 生成导入模板（含一行示例数据）并写入输出流 */
    void template(OutputStream outputStream);
}