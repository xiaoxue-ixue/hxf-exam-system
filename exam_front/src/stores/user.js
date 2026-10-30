import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 初始值从 localStorage 恢复，刷新后仍能读到登录信息
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  function setToken(t) {
    token.value = t
    if (t) localStorage.setItem('token', t)
    else localStorage.removeItem('token')
  }

  function setUserInfo(info) {
    userInfo.value = info
    if (info) localStorage.setItem('userInfo', JSON.stringify(info))
    else localStorage.removeItem('userInfo')
  }

  function logout() {
    setToken('')
    setUserInfo(null)
  }

  return { token, userInfo, setToken, setUserInfo, logout }
})