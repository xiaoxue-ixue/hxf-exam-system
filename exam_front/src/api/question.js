import axios from 'axios'
import request from '../utils/request'

// ===== 分类管理 =====
export function listCategoryTree() { return request.get('/category/tree') }
export function addCategory(data) { return request.post('/category', data) }
export function updateCategory(data) { return request.put('/category', data) }
export function deleteCategory(id) { return request.delete(`/category/${id}`) }

// ===== 题目管理 =====
export function pageQuestions(params) { return request.get('/question/page', { params }) }
export function addQuestion(data) { return request.post('/question', data) }
export function updateQuestion(data) { return request.put('/question', data) }
export function deleteQuestion(id) { return request.delete(`/question/${id}`) }

// ===== 批量导入导出 =====

// 批量导入（文件上传；FormData 交给 axios 自动设置 Content-Type 与 boundary）
export function importQuestions(file) {
    const formData = new FormData()
    formData.append('file', file)   // 字段名必须与后端 @RequestParam("file") 一致
    // 单独放宽超时：共享的 request 实例只给 10s，而上传上限已放到 10MB，
    // 几千行的 Excel 逐行解析 + 入库很容易超过 10s，否则前端先报「网络错误」而后端其实还在跑
    return request.post('/question/import', formData, { timeout: 60000 })
}

// 导出题目（Excel 下载：后端直接返回文件流，不走统一响应拦截器）
export function exportQuestions(params) {
    return downloadFile('/question/export', params, '题目列表.xlsx')
}

// 下载导入模板
export function downloadTemplate() {
    return downloadFile('/question/template', null, '题目导入模板.xlsx')
}

// 通用文件下载：用原生 axios 请求二进制流，再通过 <a> 标签触发浏览器下载。
// 不加 export：它是实现细节，不是对外能力。
async function downloadFile(url, params, filename) {
    const res = await axios.get('/api' + url, {   // 原生 axios 没有 baseURL，必须自己补 /api
        params,
        responseType: 'blob',                       // 让 axios 按二进制处理，否则内容会被破坏
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },  // 原生 axios 没有拦截器代劳
    })
    const blob = new Blob([res.data])
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = filename
    link.click()
    URL.revokeObjectURL(link.href)                // 成对释放，否则 Blob 内存不回收
}