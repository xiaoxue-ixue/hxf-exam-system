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