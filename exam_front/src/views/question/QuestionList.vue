<template>
  <div>
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="题型">
          <el-select v-model="query.type" clearable placeholder="全部" style="width: 110px">
            <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-cascader
              v-model="query.categoryId"
              :options="categoryTree"
              :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: true, emitPath: false }"
              clearable
              placeholder="全部"
              style="width: 180px"
              @change="loadData"
          />
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="query.difficulty" clearable placeholder="全部" style="width: 110px">
            <el-option v-for="d in difficultyOptions" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="搜索题干" clearable style="width: 180px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="openDialog()">新增题目</el-button>
      </div>
      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="题型" width="80">
          <template #default="{ row }">
            <el-tag :type="typeTagMap[row.type]" size="small">{{ typeNameMap[row.type] || row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="110" />
        <el-table-column label="题干" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ row.stem }}</template>
        </el-table-column>
        <el-table-column label="难度" width="80">
          <template #default="{ row }">
            <el-tag :type="diffTagMap[row.difficulty]" size="small">{{ difficultyMap[row.difficulty] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分值" width="60" />
        <el-table-column prop="creatorName" label="创建人" width="90" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click="handlePreview(row)">预览</el-button>
            <el-button size="small" link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
          class="pagination"
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="loadData"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑题目' : '新增题目'" width="680px" top="5vh">
      <el-form :model="form" label-width="80px">
        <el-form-item label="题型">
          <el-select v-model="form.type" :disabled="!!form.id" style="width: 200px" @change="onTypeChange">
            <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-cascader
              v-model="form.categoryId"
              :options="categoryTree"
              :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: true, emitPath: false }"
              clearable
              placeholder="请选择分类"
              style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="题干">
          <el-input v-model="form.stem" type="textarea" :rows="3" placeholder="请输入题干" />
        </el-form-item>

        <!-- 选择题：选项 + 正确答案 -->
        <el-form-item label="选项" v-if="isChoiceType">
          <div v-for="(opt, idx) in optionList" :key="idx" class="option-row">
            <span class="option-label">{{ opt.label }}.</span>
            <el-input v-model="opt.text" placeholder="请输入选项内容" />
          </div>
          <div v-if="form.type === 'SINGLE'" style="margin-top: 10px">
            <span>正确答案：</span>
            <el-radio-group v-model="singleCorrect">
              <el-radio v-for="opt in filledOptions" :key="opt.label" :value="opt.label">{{ opt.label }}</el-radio>
            </el-radio-group>
          </div>
          <div v-if="form.type === 'MULTI'" style="margin-top: 10px">
            <span>正确答案（可多选）：</span>
            <el-checkbox-group v-model="multiCorrect">
              <el-checkbox v-for="opt in filledOptions" :key="opt.label" :value="opt.label">{{ opt.label }}</el-checkbox>
            </el-checkbox-group>
          </div>
        </el-form-item>

        <!-- 判断题 -->
        <el-form-item label="正确答案" v-if="form.type === 'JUDGE'">
          <el-radio-group v-model="judgeAnswer">
            <el-radio value="A">对</el-radio>
            <el-radio value="B">错</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 填空题：多个空 -->
        <el-form-item label="答案" v-if="form.type === 'FILL'">
          <div v-for="(ans, i) in fillAnswers" :key="i" class="fill-row">
            <el-input v-model="fillAnswers[i]" :placeholder="`第 ${i + 1} 个空的答案`">
              <template #prepend>空 {{ i + 1 }}</template>
            </el-input>
            <el-button v-if="fillAnswers.length > 1" size="small" type="danger" circle @click="fillAnswers.splice(i, 1)">×</el-button>
          </div>
          <el-button size="small" @click="fillAnswers.push('')">+ 添加空</el-button>
        </el-form-item>

        <!-- 简答题 -->
        <el-form-item label="参考答案" v-if="form.type === 'SHORT'">
          <el-input v-model="form.answer" type="textarea" :rows="4" placeholder="请输入参考答案" />
        </el-form-item>

        <el-form-item label="解析">
          <el-input v-model="form.analysis" type="textarea" :rows="2" placeholder="请输入解析（选填）" />
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="form.difficulty" style="width: 150px">
            <el-option v-for="d in difficultyOptions" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="分值">
          <el-input-number v-model="form.score" :min="1" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>

    <!-- 预览弹窗 -->
    <el-dialog v-model="previewVisible" title="题目预览" width="560px">
      <template v-if="previewRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="题型">
            <el-tag :type="typeTagMap[previewRow.type]" size="small">{{ typeNameMap[previewRow.type] }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="分值">{{ previewRow.score }} 分</el-descriptions-item>
          <el-descriptions-item label="难度">
            <el-tag :type="diffTagMap[previewRow.difficulty]" size="small">{{ difficultyMap[previewRow.difficulty] }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="分类">{{ previewRow.categoryName || '-' }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin: 16px 0 8px;">题干</h4>
        <p style="white-space: pre-wrap;">{{ previewRow.stem }}</p>
        <template v-if="previewOptions.length">
          <h4 style="margin: 16px 0 8px;">选项</h4>
          <p v-for="opt in previewOptions" :key="opt.label">{{ opt.label }}. {{ opt.text }}</p>
        </template>
        <h4 style="margin: 16px 0 8px;">正确答案</h4>
        <p>{{ previewAnswer }}</p>
        <template v-if="previewRow.analysis">
          <h4 style="margin: 16px 0 8px;">解析</h4>
          <p style="white-space: pre-wrap;">{{ previewRow.analysis }}</p>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listCategoryTree, pageQuestions, addQuestion, updateQuestion, deleteQuestion } from '../../api/question'

// 题型：后端以整数存储并返回（QuestionVO.type），前端表单使用字符串枚举
const TYPE_INT_TO_STR = { 1: 'SINGLE', 2: 'MULTI', 3: 'JUDGE', 4: 'FILL', 5: 'SHORT' }
const typeNameMap = { 1: '单选题', 2: '多选题', 3: '判断题', 4: '填空题', 5: '简答题' }
const typeTagMap = { 1: 'primary', 2: 'warning', 3: 'success', 4: 'danger', 5: 'info' }
const typeOptions = [
  { label: '单选题', value: 'SINGLE' }, { label: '多选题', value: 'MULTI' }, { label: '判断题', value: 'JUDGE' },
  { label: '填空题', value: 'FILL' }, { label: '简答题', value: 'SHORT' },
]
const difficultyMap = { 1: '简单', 2: '中等', 3: '困难' }
const diffTagMap = { 1: 'success', 2: 'warning', 3: 'danger' }
const difficultyOptions = [
  { label: '简单', value: 1 }, { label: '中等', value: 2 }, { label: '困难', value: 3 },
]

const loading = ref(false)
const list = ref([])
const total = ref(0)
const categoryTree = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, type: null, categoryId: null, difficulty: null, keyword: null })

// 对话框
const dialogVisible = ref(false)
const form = reactive({ id: null, type: 'SINGLE', categoryId: null, stem: '', options: '', answer: '', analysis: '', difficulty: 1, score: 5 })
const optionList = ref([])
const singleCorrect = ref('')
const multiCorrect = ref([])
const judgeAnswer = ref('A')
const fillAnswers = ref([''])
const isChoiceType = computed(() => form.type === 'SINGLE' || form.type === 'MULTI')
const filledOptions = computed(() => optionList.value.filter(o => o.text))

// 预览
const previewVisible = ref(false)
const previewRow = ref(null)
const previewOptions = computed(() => {
  if (!previewRow.value?.options) return []
  try { return JSON.parse(previewRow.value.options) } catch { return [] }
})
const previewAnswer = computed(() => {
  if (!previewRow.value) return ''
  const row = previewRow.value
  const typeStr = TYPE_INT_TO_STR[row.type]
  if (typeStr === 'JUDGE') return row.answer === 'A' ? '对' : '错'
  if (typeStr === 'MULTI') return row.answer ? row.answer.split(',').join('、') : ''
  return row.answer
})

async function loadData() {
  loading.value = true
  try {
    const data = await pageQuestions(query)
    list.value = data.records
    total.value = data.total
  } finally { loading.value = false }
}

async function loadCategories() {
  categoryTree.value = await listCategoryTree()
}

function handleSearch() { query.pageNum = 1; loadData() }

function handleReset() {
  query.type = null; query.categoryId = null; query.difficulty = null
  query.keyword = null; query.pageNum = 1
  loadData()
}

// 切换题型时重置答案，防止残留
function onTypeChange() {
  singleCorrect.value = ''
  multiCorrect.value = []
  judgeAnswer.value = 'A'
  fillAnswers.value = ['']
  form.answer = ''
  if (isChoiceType.value) initOptions()
}

function openDialog(row) {
  if (row) {
    // 后端返回的 type 是整数，先转回表单使用的字符串枚举
    const typeStr = TYPE_INT_TO_STR[row.type]
    Object.assign(form, {
      id: row.id, type: typeStr, categoryId: row.categoryId, stem: row.stem,
      options: row.options, answer: row.answer, analysis: row.analysis,
      difficulty: row.difficulty, score: row.score,
    })
    if (typeStr === 'SINGLE' || typeStr === 'MULTI') {
      parseOptions(row.options)
      if (typeStr === 'SINGLE') singleCorrect.value = row.answer || ''
      else multiCorrect.value = row.answer ? row.answer.split(',') : []
    } else if (typeStr === 'JUDGE') {
      judgeAnswer.value = row.answer || 'A'
    } else if (typeStr === 'FILL') {
      fillAnswers.value = row.answer ? row.answer.split(',') : ['']
    }
  } else {
    Object.assign(form, { id: null, type: 'SINGLE', categoryId: null, stem: '', options: '', answer: '', analysis: '', difficulty: 1, score: 5 })
    initOptions(); singleCorrect.value = ''; multiCorrect.value = []; judgeAnswer.value = 'A'; fillAnswers.value = ['']
  }
  dialogVisible.value = true
}

function initOptions() {
  optionList.value = [
    { label: 'A', text: '' }, { label: 'B', text: '' },
    { label: 'C', text: '' }, { label: 'D', text: '' },
  ]
}

// 编辑时把 JSON 选项字符串解析回 { label, text } 数组
function parseOptions(json) {
  try {
    const parsed = JSON.parse(json)
    optionList.value = parsed.map((o, i) =>
        typeof o === 'object'
            ? { label: o.label || String.fromCharCode(65 + i), text: o.text || '' }
            : { label: String.fromCharCode(65 + i), text: String(o) })
  } catch { initOptions() }
}

async function handleSave() {
  if (!form.stem) { ElMessage.warning('请输入题干'); return }
  if (!form.categoryId) { ElMessage.warning('请选择分类'); return }
  const data = { ...form }
  if (form.type === 'SINGLE' || form.type === 'MULTI') {
    // 选项内容可能被清空，此时答案不能再指向它（否则库里出现 answer 不在 options 里的脏数据）
    const validLabels = optionList.value.filter(o => o.text).map(o => o.label)
    if (form.type === 'SINGLE' && singleCorrect.value && !validLabels.includes(singleCorrect.value)) {
      ElMessage.warning('正确答案必须是已填写内容的选项')
      return
    }
    if (form.type === 'MULTI' && multiCorrect.value.some(l => !validLabels.includes(l))) {
      ElMessage.warning('正确答案中有选项内容为空')
      return
    }
    data.options = JSON.stringify(optionList.value.filter(o => o.text))
    // 排序后再存，保证 "C,A" 与 "A,C" 落库一致（第 12 周判分要靠它）
    data.answer = form.type === 'SINGLE' ? singleCorrect.value : [...multiCorrect.value].sort().join(',')
    if (!data.answer) { ElMessage.warning('请选择正确答案'); return }
  } else if (form.type === 'JUDGE') {
    data.answer = judgeAnswer.value
  } else if (form.type === 'FILL') {
    data.answer = fillAnswers.value.filter(a => a.trim()).join(',')
    if (!data.answer) { ElMessage.warning('请输入答案'); return }
  }
  if (data.id) { await updateQuestion(data); ElMessage.success('修改成功') }
  else { await addQuestion(data); ElMessage.success('新增成功') }
  dialogVisible.value = false
  loadData()
}

function handlePreview(row) {
  previewRow.value = row
  previewVisible.value = true
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该题目吗？', '提示', { type: 'warning' })
  await deleteQuestion(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => { loadData(); loadCategories() })
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.toolbar { margin-bottom: 16px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
.option-row { display: flex; align-items: center; margin-bottom: 8px; width: 100%; }
.option-label { width: 30px; font-weight: bold; }
.fill-row { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; width: 100%; }
</style>