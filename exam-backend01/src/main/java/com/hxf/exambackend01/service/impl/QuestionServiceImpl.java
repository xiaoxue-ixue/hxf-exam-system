package com.hxf.exambackend01.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxf.exambackend01.common.BusinessException;
import com.hxf.exambackend01.dto.QuestionExcelDTO;
import com.hxf.exambackend01.dto.QuestionQueryDTO;
import com.hxf.exambackend01.dto.QuestionSaveDTO;
import com.hxf.exambackend01.entity.Question;
import com.hxf.exambackend01.entity.QuestionCategory;
import com.hxf.exambackend01.entity.SysUser;
import com.hxf.exambackend01.mapper.QuestionCategoryMapper;
import com.hxf.exambackend01.mapper.QuestionMapper;
import com.hxf.exambackend01.mapper.SysUserMapper;
import com.hxf.exambackend01.service.QuestionService;
import com.hxf.exambackend01.vo.ImportErrorVO;
import com.hxf.exambackend01.vo.QuestionImportResultVO;
import com.hxf.exambackend01.vo.QuestionVO;
import lombok.Data;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;
    private final QuestionCategoryMapper categoryMapper;
    private final SysUserMapper sysUserMapper;
    private final ObjectMapper objectMapper;

    public QuestionServiceImpl(QuestionMapper questionMapper,
                               QuestionCategoryMapper categoryMapper,
                               SysUserMapper sysUserMapper,
                               ObjectMapper objectMapper) {
        this.questionMapper = questionMapper;
        this.categoryMapper = categoryMapper;
        this.sysUserMapper = sysUserMapper;
        this.objectMapper = objectMapper;
    }

    /** 题型字符串 -> 整数（前端表单用） */
    private static final Map<String, Integer> TYPE_STR_TO_INT = Map.of(
            "SINGLE", 1, "MULTI", 2, "JUDGE", 3, "FILL", 4, "SHORT", 5);
    /** 题型中文名 -> 整数（Excel 导入用） */
    private static final Map<String, Integer> TYPE_NAME_TO_INT = Map.of(
            "单选题", 1, "多选题", 2, "判断题", 3, "填空题", 4, "简答题", 5);
    /** 难度中文名 -> 整数（Excel 导入用） */
    private static final Map<String, Integer> DIFF_NAME_TO_INT = Map.of(
            "简单", 1, "中等", 2, "困难", 3);
    /** 下标 0 占位，让「下标 = 题型编号」 */
    private static final String[] TYPE_NAMES = {"", "单选题", "多选题", "判断题", "填空题", "简答题"};
    /** 下标 0 占位，让「下标 = 难度编号」 */
    private static final String[] DIFF_NAMES = {"", "简单", "中等", "困难"};
    /** 导入模板的必需列：缺任意一列都无法正常导入（EasyExcel 按表头名匹配列） */
    private static final List<String> REQUIRED_HEADS = List.of("题型", "题干", "分类", "正确答案");

    // ================= 分页 / 详情 =================

    @Override
    public Page<QuestionVO> page(QuestionQueryDTO query) {
        Page<Question> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Question> qPage = questionMapper.selectPage(page, buildWrapper(query));

        // 一次性把分类名、用户名做成 Map，toVO 里不再查库（避免 N+1）
        Map<Long, String> categoryNameMap = getCategoryNameMap();
        Map<Long, String> userNameMap = getUserNameMap();

        Page<QuestionVO> voPage = new Page<>(qPage.getCurrent(), qPage.getSize(), qPage.getTotal());
        voPage.setRecords(qPage.getRecords().stream()
                .map(q -> toVO(q, categoryNameMap, userNameMap))
                .collect(Collectors.toList()));
        return voPage;
    }

    /** 组装筛选条件：分页查询与导出共用（导出复用它是第 7 周的设计要点） */
    private LambdaQueryWrapper<Question> buildWrapper(QuestionQueryDTO query) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        // 题型：前端传字符串，先转整数再拼条件；非法值忽略（退化为不筛选）
        if (StringUtils.hasText(query.getType()) && TYPE_STR_TO_INT.containsKey(query.getType())) {
            wrapper.eq(Question::getType, TYPE_STR_TO_INT.get(query.getType()));
        }
        wrapper.eq(query.getCategoryId() != null, Question::getCategoryId, query.getCategoryId())
                .eq(query.getDifficulty() != null, Question::getDifficulty, query.getDifficulty())
                .like(StringUtils.hasText(query.getKeyword()), Question::getContent, query.getKeyword())
                .orderByDesc(Question::getId);
        return wrapper;
    }

    @Override
    public QuestionVO detail(Long id) {
        Question q = questionMapper.selectById(id);
        if (q == null) {
            throw new BusinessException("题目不存在");
        }
        return toVO(q, getCategoryNameMap(), getUserNameMap());
    }

    // ================= 手动增删改 =================

    @Override
    public void save(QuestionSaveDTO dto) {
        validate(dto);
        Question question = toEntity(dto);
        // 创建人由后端从 token 里取，不能前端传
        question.setCreatorId(getCurrentUserId());
        questionMapper.insert(question);
    }

    @Override
    public void update(QuestionSaveDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("题目 ID 不能为空");
        }
        validate(dto);
        Question question = toEntity(dto);
        // toEntity 没设 creatorId / createdAt，updateById 会跳过 null 字段，创建信息保持不变
        questionMapper.updateById(question);
    }

    @Override
    public void delete(Long id) {
        // @TableLogic 生效，实际执行 UPDATE ... SET deleted=1
        questionMapper.deleteById(id);
    }

    // ================= 批量导入 =================

    @Override
    public QuestionImportResultVO importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要导入的 Excel 文件");
        }

        // 一次性加载「分类名称 -> id」映射，避免逐行查库
        Map<String, Long> categoryIdByName = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(QuestionCategory::getName, QuestionCategory::getId, (a, b) -> a));
        List<Question> successList = new ArrayList<>();
        List<ImportErrorVO> errors = new ArrayList<>();

        // 逐行读取的监听器：合法行收集起来，非法行记录行号与原因，互不影响
        AnalysisEventListener<QuestionExcelDTO> listener = new AnalysisEventListener<>() {
            /** 表头：列下标 -> 表头名，用于把「第几列出错」翻译成「哪一列出错」 */
            private Map<Integer, String> headMap = new HashMap<>();

            @Override
            public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                this.headMap = headMap;
                // EasyExcel 是按表头名匹配列的，少了列不会报错，只会让该列读不到值，
                // 最后变成一行行莫名其妙的校验失败，所以读表头时先把缺列找出来
                List<String> missing = REQUIRED_HEADS.stream()
                        .filter(head -> !headMap.containsValue(head))
                        .collect(Collectors.toList());
                if (!missing.isEmpty()) {
                    throw new BusinessException("表头与模板不一致，缺少列：" + String.join("、", missing));
                }
            }

            @Override
            public void invoke(QuestionExcelDTO data, AnalysisContext context) {
                // rowIndex 从 0 开始（0 是表头），+1 得到 Excel 界面上的行号
                int row = context.readRowHolder().getRowIndex() + 1;
                if (isBlankRow(data)) {
                    return;   // 整行全空：既不算成功也不算失败
                }
                try {
                    successList.add(convertToQuestion(data, categoryIdByName));
                } catch (BusinessException e) {
                    // 只捕获业务异常，真正的 bug 不能被吞成「第 N 行的某种原因」
                    errors.add(new ImportErrorVO(row, e.getMessage()));
                }
            }

            /**
             * 单元格类型转换失败时会走到这里（比如「分值」一列填了文字）。
             * 记下该行的原因后正常返回，EasyExcel 会继续读下一行，
             * 不会因为一个单元格写错就中断整份文件的导入。
             */
            @Override
            public void onException(Exception exception, AnalysisContext context) throws Exception {
                if (exception instanceof ExcelDataConvertException e) {
                    String head = headMap.getOrDefault(e.getColumnIndex(),
                            "第 " + (e.getColumnIndex() + 1) + " 列");
                    String value = e.getCellData() == null ? "" : e.getCellData().getStringValue();
                    errors.add(new ImportErrorVO(e.getRowIndex() + 1,
                            "「" + head + "」列的内容「" + value + "」不是有效数字"));
                    return;      // 关键：正常返回 = 告诉 EasyExcel「继续读下一行」
                }
                throw exception; // 其它异常仍然抛出，不掩盖真正的 bug
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
            }
        };

        try {
            EasyExcel.read(file.getInputStream(), QuestionExcelDTO.class, listener).sheet().doRead();
        } catch (IOException e) {
            throw new BusinessException("读取 Excel 文件失败，请确认文件格式正确");
        } catch (BusinessException e) {
            // 监听器里抛出的业务异常（比如表头缺列）原样往外传
            throw e;
        } catch (ExcelAnalysisException e) {
            throw new BusinessException("文件格式不正确，请上传 .xlsx 或 .xls 文件");
        }

        // 一行都没读到：多半是上传了非 Excel 文件，或表头被改坏
        if (successList.isEmpty() && errors.isEmpty()) {
            throw new BusinessException("文件中没有读到任何题目，请使用模板文件且不要修改表头");
        }

        // 合法数据统一入库；当前用户只查一次（第 6 周埋的伏笔）
        Long creatorId = getCurrentUserId();
        for (Question q : successList) {
            q.setCreatorId(creatorId);
            questionMapper.insert(q);
        }

        QuestionImportResultVO result = new QuestionImportResultVO();
        result.setSuccessCount(successList.size());
        result.setFailCount(errors.size());
        result.setErrors(errors);
        return result;
    }

    /** 把一行 Excel 数据转换为 Question 实体，不合法时抛异常说明原因 */
    private Question convertToQuestion(QuestionExcelDTO data, Map<String, Long> categoryIdByName) {
        Question q = new Question();

        // 1. 题型：中文 -> 整数。防 null：空单元格读出来是 null，
        //    而 Map.of(...) 生成的不可变 Map 遇到 null 键会抛 NPE
        Integer type = TYPE_NAME_TO_INT.get(data.getTypeName() == null ? "" : data.getTypeName().trim());
        if (type == null) {
            throw new BusinessException("题型「" + data.getTypeName()
                    + "」不合法，应为：单选题/多选题/判断题/填空题/简答题");
        }
        q.setType(type);

        // 2. 题干
        if (!StringUtils.hasText(data.getContent())) {
            throw new BusinessException("题干不能为空");
        }
        q.setContent(data.getContent().trim());

        // 3. 分类：名称 -> id（必填，与模板说明、前端表单校验保持一致）
        if (!StringUtils.hasText(data.getCategoryName())) {
            throw new BusinessException("分类不能为空");
        }
        Long categoryId = categoryIdByName.get(data.getCategoryName().trim());
        if (categoryId == null) {
            throw new BusinessException("分类「" + data.getCategoryName()
                    + "」不存在，请先在「题目分类」中创建");
        }
        q.setCategoryId(categoryId);

        // 4. 选项 + 答案
        String answer = data.getAnswer() == null ? "" : data.getAnswer().trim();
        if (type == 1 || type == 2) {
            // 单选 / 多选：组装选项 JSON；先拼选项再校验答案（选项不足时答案对错已无意义）
            List<OptionItem> options = new ArrayList<>();
            addOption(options, "A", data.getOptionA());
            addOption(options, "B", data.getOptionB());
            addOption(options, "C", data.getOptionC());
            addOption(options, "D", data.getOptionD());
            if (options.size() < 2) {
                throw new BusinessException("选择题至少需要两个选项");
            }
            q.setOptions(toJson(options));
            if (!StringUtils.hasText(answer)) {
                throw new BusinessException("正确答案不能为空");
            }
            // 归一化：中文逗号转英文、统一大写
            String upper = answer.replace("，", ",").toUpperCase();
            if (type == 1) {
                if (!upper.matches("[A-D]")) {
                    throw new BusinessException("单选题答案应为 A、B、C、D 之一");
                }
                q.setAnswer(upper);
            } else {
                // 多选是「集合」：去重 + 排序，C,A 与 A,C 落库一致
                if (!upper.matches("([A-D],)*[A-D]")) {
                    throw new BusinessException("多选题答案格式应为 A,C 之类");
                }
                q.setAnswer(Arrays.stream(upper.split(","))
                        .distinct().sorted().collect(Collectors.joining(",")));
            }
        } else if (type == 3) {
            // 判断题：对 -> A，错 -> B（与第 6 周界面保存格式一致）
            if ("对".equals(answer) || "正确".equals(answer)
                    || "A".equalsIgnoreCase(answer) || "√".equals(answer)) {
                q.setAnswer("A");
            } else if ("错".equals(answer) || "错误".equals(answer)
                    || "B".equalsIgnoreCase(answer) || "×".equals(answer)) {
                q.setAnswer("B");
            } else {
                throw new BusinessException("判断题答案应为「对」或「错」");
            }
            q.setOptions(null);
        } else {
            // 填空 / 简答：不排序（填空是有序的，第 1 个空与第 2 个空不能互换）
            if (!StringUtils.hasText(answer)) {
                throw new BusinessException("答案不能为空");
            }
            q.setAnswer(answer);
            q.setOptions(null);
        }

        // 5. 难度：中文 -> 整数；非法值按默认值处理（选填字段宽容）
        Integer difficulty = DIFF_NAME_TO_INT.get(
                data.getDifficultyName() == null ? "" : data.getDifficultyName().trim());
        q.setDifficulty(difficulty != null ? difficulty : 1);

        // 6. 分值：默认 5
        q.setScore(data.getScore() != null ? data.getScore() : 5);

        // 7. 解析
        q.setAnalysis(data.getAnalysis());
        return q;
    }

    private void addOption(List<OptionItem> options, String label, String text) {
        if (StringUtils.hasText(text)) {
            OptionItem item = new OptionItem();
            item.setLabel(label);
            item.setText(text.trim());
            options.add(item);
        }
    }

    /** 11 列全空视为空行（静默跳过） */
    private boolean isBlankRow(QuestionExcelDTO d) {
        return !StringUtils.hasText(d.getTypeName())
                && !StringUtils.hasText(d.getContent())
                && !StringUtils.hasText(d.getCategoryName())
                && !StringUtils.hasText(d.getOptionA())
                && !StringUtils.hasText(d.getOptionB())
                && !StringUtils.hasText(d.getOptionC())
                && !StringUtils.hasText(d.getOptionD())
                && !StringUtils.hasText(d.getAnswer())
                && !StringUtils.hasText(d.getDifficultyName())
                && d.getScore() == null
                && !StringUtils.hasText(d.getAnalysis());
    }

    // ================= 导出 / 模板 =================

    @Override
    public void export(QuestionQueryDTO query, OutputStream outputStream) {
        // 导出不分页：把当前筛选条件下的全部题目取走
        List<Question> list = questionMapper.selectList(buildWrapper(query));
        Map<Long, String> categoryNameMap = getCategoryNameMap();
        List<QuestionExcelDTO> excelList = list.stream()
                .map(q -> toExcelDTO(q, categoryNameMap))
                .collect(Collectors.toList());
        EasyExcel.write(outputStream, QuestionExcelDTO.class).sheet("题目").doWrite(excelList);
    }

    /** 把库里的格式还原成「给人填的格式」（导出是导入的逆过程） */
    private QuestionExcelDTO toExcelDTO(Question q, Map<Long, String> categoryNameMap) {
        QuestionExcelDTO dto = new QuestionExcelDTO();
        // 数组下标先做范围检查：库里若有 type=6 或 null 的脏数据，直接取下标会让整个导出崩掉
        dto.setTypeName(q.getType() != null && q.getType() >= 1 && q.getType() <= 5
                ? TYPE_NAMES[q.getType()] : "");
        dto.setContent(q.getContent());
        dto.setCategoryName(categoryNameMap.getOrDefault(q.getCategoryId(), ""));
        // 选项 JSON -> 拆成 A/B/C/D 四列
        for (OptionItem opt : parseOptions(q.getOptions())) {
            String label = opt.getLabel();
            if ("A".equals(label)) {
                dto.setOptionA(opt.getText());
            } else if ("B".equals(label)) {
                dto.setOptionB(opt.getText());
            } else if ("C".equals(label)) {
                dto.setOptionC(opt.getText());
            } else if ("D".equals(label)) {
                dto.setOptionD(opt.getText());
            }
        }
        // 判断题：A/B -> 对/错
        if (q.getType() != null && q.getType() == 3) {
            dto.setAnswer("A".equals(q.getAnswer()) ? "对" : "错");
        } else {
            dto.setAnswer(q.getAnswer());
        }
        dto.setDifficultyName(q.getDifficulty() != null && q.getDifficulty() >= 1 && q.getDifficulty() <= 3
                ? DIFF_NAMES[q.getDifficulty()] : "");
        dto.setScore(q.getScore());
        dto.setAnalysis(q.getAnalysis());
        return dto;
    }

    @Override
    public void template(OutputStream outputStream) {
        // 写一行示例数据：既展示表头，又把「选项怎么写、答案怎么写」示范一遍
        QuestionExcelDTO example = new QuestionExcelDTO();
        example.setTypeName("单选题");
        example.setContent("Java 中以下哪个不是基本数据类型？");
        // 故意用一个库里通常不存在的分类：模板是「格式示范」，不是「可导入数据」
        example.setCategoryName("Java 基础");
        example.setOptionA("int");
        example.setOptionB("double");
        example.setOptionC("String");
        example.setOptionD("boolean");
        example.setAnswer("C");
        example.setDifficultyName("简单");
        example.setScore(5);
        example.setAnalysis("String 是引用数据类型，不属于 8 种基本数据类型");
        EasyExcel.write(outputStream, QuestionExcelDTO.class).sheet("题目").doWrite(List.of(example));
    }

    // ================= 私有工具方法 =================

    /** 选项内部类：label 为 A/B/C/D，text 为选项内容 */
    @Data
    private static class OptionItem {
        private String label;
        private String text;
    }

    /** 写路径：失败要抛（造不出要入库的数据，不能忍） */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new BusinessException("选项数据序列化失败");
        }
    }

    /** 读路径：失败返回空列表（历史上可能有格式不对的旧数据，不能因此让整个导出挂掉） */
    private List<OptionItem> parseOptions(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<OptionItem>>() {
            });
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private QuestionVO toVO(Question q, Map<Long, String> categoryNameMap, Map<Long, String> userNameMap) {
        QuestionVO vo = new QuestionVO();
        vo.setId(q.getId());
        vo.setType(q.getType());
        if (q.getType() != null && q.getType() >= 1 && q.getType() <= 5) {
            vo.setTypeName(TYPE_NAMES[q.getType()]);
        }
        vo.setCategoryId(q.getCategoryId());
        vo.setCategoryName(categoryNameMap.getOrDefault(q.getCategoryId(), ""));
        vo.setStem(q.getContent());
        vo.setOptions(q.getOptions());
        vo.setAnswer(q.getAnswer());
        vo.setAnalysis(q.getAnalysis());
        vo.setDifficulty(q.getDifficulty());
        Map<Integer, String> diffMap = Map.of(1, "简单", 2, "中等", 3, "困难");
        vo.setDifficultyName(diffMap.getOrDefault(q.getDifficulty(), "未知"));
        vo.setScore(q.getScore());
        vo.setCreatorId(q.getCreatorId());
        vo.setCreatorName(userNameMap.getOrDefault(q.getCreatorId(), ""));
        vo.setCreatedAt(q.getCreatedAt());
        return vo;
    }

    private void validate(QuestionSaveDTO dto) {
        if (!StringUtils.hasText(dto.getStem())) {
            throw new BusinessException("题干不能为空");
        }
        if (!StringUtils.hasText(dto.getType()) || !TYPE_STR_TO_INT.containsKey(dto.getType())) {
            throw new BusinessException("题型不合法");
        }
        int typeInt = TYPE_STR_TO_INT.get(dto.getType());
        if (typeInt <= 2 && !StringUtils.hasText(dto.getOptions())) {
            throw new BusinessException("选择题必须填写选项");
        }
        if (!StringUtils.hasText(dto.getAnswer())) {
            throw new BusinessException("答案不能为空");
        }
    }

    /** 从 SecurityContext 取当前登录用户名，再查库拿 userId */
    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof String username) {
            SysUser user = sysUserMapper.selectOne(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
            return user != null ? user.getId() : null;
        }
        return null;
    }

    private Question toEntity(QuestionSaveDTO dto) {
        Question q = new Question();
        q.setId(dto.getId());
        // 字符串题型转整数；非法值兜底成单选（validate 已挡住非法值）
        if (dto.getType() != null && TYPE_STR_TO_INT.containsKey(dto.getType())) {
            q.setType(TYPE_STR_TO_INT.get(dto.getType()));
        } else {
            q.setType(1);
        }
        q.setCategoryId(dto.getCategoryId());
        // stem -> content
        q.setContent(dto.getStem());
        q.setOptions(dto.getOptions());
        q.setAnswer(dto.getAnswer());
        q.setAnalysis(dto.getAnalysis());
        q.setDifficulty(dto.getDifficulty() != null ? dto.getDifficulty() : 1);
        q.setScore(dto.getScore() != null ? dto.getScore() : 5);
        return q;
    }

    /** 分类 id -> 分类名 */
    private Map<Long, String> getCategoryNameMap() {
        return categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(QuestionCategory::getId, QuestionCategory::getName, (a, b) -> a));
    }

    /** 用户 id -> 显示名（优先真实姓名，无则用账号） */
    private Map<Long, String> getUserNameMap() {
        List<SysUser> users = sysUserMapper.selectList(null);
        return users.stream()
                .collect(Collectors.toMap(SysUser::getId,
                        u -> u.getRealName() != null && StringUtils.hasText(u.getRealName())
                                ? u.getRealName() : u.getUsername(),
                        (a, b) -> a));
    }
}