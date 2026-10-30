 import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({ baseURL: '/api', timeout: 10000 })

// 请求拦截器：自动携带 token
request.interceptors.request.use((config) => {
    const token = localStorage.getItem('token')
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
})

// 响应拦截器：统一处理 {code, message, data}
request.interceptors.response.use(
    (response) => {
        const res = response.data
        if (res.code === 200) return res.data
        ElMessage.error(res.message || '请求失败')
        return Promise.reject(new Error(res.message || '请求失败'))
    },
    (error) => {
        ElMessage.error(error.response?.data?.message || '网络错误')
        return Promise.reject(error)
    }
)

export default request





