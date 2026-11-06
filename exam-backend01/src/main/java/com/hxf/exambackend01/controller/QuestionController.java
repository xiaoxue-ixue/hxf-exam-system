package com.hxf.exambackend01.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hxf.exambackend01.common.R;
import com.hxf.exambackend01.dto.QuestionQueryDTO;
import com.hxf.exambackend01.dto.QuestionSaveDTO;
import com.hxf.exambackend01.service.QuestionService;
import com.hxf.exambackend01.vo.QuestionImportResultVO;
import com.hxf.exambackend01.vo.QuestionVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 题目管理接口。
 */
@RestController
@RequestMapping("/question")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /** 分页 + 4 个筛选条件 */
    @GetMapping("/page")
    public R<Page<QuestionVO>> page(QuestionQueryDTO query) {
        return R.success(questionService.page(query));
    }

    /** 详情；字面量路径 /page 优先于 /{id}，不会冲突 */
    @GetMapping("/{id}")
    public R<QuestionVO> detail(@PathVariable Long id) {
        return R.success(questionService.detail(id));
    }

    /** 新增 */
    @PostMapping
    public R<Void> add(@RequestBody QuestionSaveDTO dto) {
        questionService.save(dto);
        return R.success();
    }

    /** 编辑 */
    @PutMapping
    public R<Void> update(@RequestBody QuestionSaveDTO dto) {
        questionService.update(dto);
        return R.success();
    }

    /** 删除（逻辑删除） */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return R.success();
    }

    /** 批量导入：接收 Excel 文件，返回逐行导入结果 */
    @PostMapping("/import")
    public R<QuestionImportResultVO> importExcel(@RequestParam("file") MultipartFile file) {
        return R.success(questionService.importExcel(file));
    }

    /** 导出：按筛选条件把题目写进 Excel 并触发浏览器下载 */
    @GetMapping("/export")
    public void export(QuestionQueryDTO query, HttpServletResponse response) throws IOException {
        setExcelResponse(response, "题目列表");
        questionService.export(query, response.getOutputStream());
    }

    /** 下载导入模板（含一行示例数据） */
    @GetMapping("/template")
    public void template(HttpServletResponse response) throws IOException {
        setExcelResponse(response, "题目导入模板");
        questionService.template(response.getOutputStream());
    }

    /** 设置 Excel 下载响应头 */
    private void setExcelResponse(HttpServletResponse response, String fileName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // Content-Disposition 只能放 ASCII，中文文件名按 RFC 5987 用 filename*=utf-8''<百分号编码>
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + encoded + ".xlsx");
    }
}