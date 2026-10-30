import request from '../utils/request'

export function pageUsers(params) {
    return request.get('/user/page', { params })
}

export function listRoles() {
    return request.get('/user/roles')
}

export function addUser(data) {
    return request.post('/user', data)
}

export function updateUser(data) {
    return request.put('/user', data)
}

export function deleteUser(id) {
    return request.delete(`/user/${id}`)
}

export function changeStatus(id, status) {
    return request.put(`/user/${id}/status/${status}`)
}

export function resetPassword(id) {
    return request.put(`/user/${id}/reset-password`)
}