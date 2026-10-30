<template>
  <div>
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd(null)">新增分类</el-button>
      </div>
      <el-tree
          :data="treeData"
          node-key="id"
          default-expand-all
          :expand-on-click-node="false"
          v-loading="loading"
          empty-text="暂无分类数据"
      >
        <template #default="{ data }">
          <span class="tree-node">
            <span>{{ data.name }}</span>
            <span class="tree-actions">
              <el-button size="small" @click.stop="handleAdd(data)">新增子分类</el-button>
              <el-button size="small" @click.stop="handleEdit(data)">编辑</el-button>
              <el-button size="small" type="danger" @click.stop="handleDelete(data)">删除</el-button>
            </span>
          </span>
        </template>
      </el-tree>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类名称">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="上级分类">
          <el-tree-select
              v-model="form.parentId"
              :data="parentOptions"
              :props="{ label: 'name', value: 'id' }"
              check-strictly
              clearable
              placeholder="不选则为顶级分类"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listCategoryTree, addCategory, updateCategory, deleteCategory } from '../../api/question'

const loading = ref(false)
const treeData = ref([])
const dialogVisible = ref(false)
const form = reactive({ id: null, name: '', parentId: null, sort: 0 })

// 上级分类候选：编辑时必须排除自己（自己节点被移除，其所有后代随之一起消失），
// 否则把「上级」选成自己的子分类会成环，buildTree 永远遍历不到这一支，节点从树上静默消失
const parentOptions = computed(() => {
  if (!form.id) return treeData.value
  const removeSelf = (nodes) => nodes
      .filter(n => n.id !== form.id)
      .map(n => ({ ...n, children: n.children ? removeSelf(n.children) : [] }))
  return removeSelf(treeData.value)
})

async function loadTree() {
  loading.value = true
  try { treeData.value = await listCategoryTree() } finally { loading.value = false }
}

function handleAdd(parent) {
  // 顶层分类用 0 作哨兵值（与 question_category.parent_id 的 NOT NULL DEFAULT 0 一致）
  Object.assign(form, { id: null, name: '', parentId: parent ? parent.id : 0, sort: 0 })
  dialogVisible.value = true
}

function handleEdit(data) {
  Object.assign(form, { id: data.id, name: data.name, parentId: data.parentId, sort: data.sort || 0 })
  dialogVisible.value = true
}

async function handleSave() {
  // 全空格也算空：不 trim 的话库里会多出一个界面上看不见、也点不到的分类
  const name = (form.name || '').trim()
  if (!name) { ElMessage.warning('请输入分类名称'); return }
  // 清空"上级分类"时 tree-select 会把值置为 null，统一归一为 0
  const data = { ...form, name, parentId: form.parentId ?? 0 }
  if (data.id) { await updateCategory(data); ElMessage.success('修改成功') }
  else { await addCategory(data); ElMessage.success('新增成功') }
  dialogVisible.value = false
  loadTree()
}

async function handleDelete(data) {
  await ElMessageBox.confirm(`确定删除分类「${data.name}」吗？`, '提示', { type: 'warning' })
  await deleteCategory(data.id)
  ElMessage.success('删除成功')
  loadTree()
}

onMounted(() => loadTree())
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
.tree-node { flex: 1; display: flex; align-items: center; justify-content: space-between; font-size: 14px; padding-right: 8px; }
.tree-actions { margin-left: 12px; }
</style>