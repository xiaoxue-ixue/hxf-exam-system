<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user.js'
import { login } from '@/api/auth.js'

// 第 4 周（下午）：登录改为调用真实后端接口 POST /api/auth/login
const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  username: '',
  password: '',
})

const error = ref('')          // 校验错误提示
const showPassword = ref(false) // 密码是否明文显示
const loading = ref(false)      // 登录中（防止重复点击）

async function handleLogin() {
  // 交互规则：账号或密码为空 → 提示，不提交
  if (!form.username.trim() || !form.password.trim()) {
    error.value = '请输入账号和密码'
    return
  }

  error.value = ''
  loading.value = true
  try {
    const data = await login({
      username: form.username.trim(),
      password: form.password,
    })
    // data 即后端返回的 LoginVO：{token, userId, username, realName, role}
    userStore.setToken(data.token)
    userStore.setUserInfo(data)
    ElMessage.success('登录成功')
    router.push('/home')
  } catch (e) {
    // 错误提示已由 request.js 响应拦截器统一弹出，这里无需再提示
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <!-- 背景装饰光斑 -->
    <span class="blob blob-1"></span>
    <span class="blob blob-2"></span>
    <span class="blob blob-3"></span>

    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- Logo：学士帽 -->
      <div class="logo">
        <svg viewBox="0 0 48 48" width="52" height="52" fill="none" stroke="currentColor"
          stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round">
          <path d="M24 8 L40 16 L24 24 L8 16 Z" />
          <path d="M14 20 v9 c0 0 5 5 10 5 s10 -5 10 -5 v-9" />
          <path d="M40 16 v10" />
        </svg>
      </div>

      <h1 class="title">在线考试系统</h1>
      <p class="subtitle">ONLINE EXAM SYSTEM</p>

      <div class="divider"></div>

      <!-- 账号输入框 -->
      <div class="field" :class="{ active: form.username !== '' }">
        <span class="icon">
          <svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor"
            stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="8" r="4" />
            <path d="M4 21 c0 -4.4 3.6 -7 8 -7 s8 2.6 8 7" />
          </svg>
        </span>
        <input v-model="form.username" type="text" placeholder="请输入账号"
          @keyup.enter="handleLogin" @input="error = ''" />
      </div>

      <!-- 密码输入框 -->
      <div class="field" :class="{ active: form.password !== '' }">
        <span class="icon">
          <svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor"
            stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
            <rect x="4" y="10" width="16" height="11" rx="2.5" />
            <path d="M8 10 V7.5 C8 4.5 10 3 12 3 s4 1.5 4 4.5 V10" />
          </svg>
        </span>
        <input v-model="form.password" :type="showPassword ? 'text' : 'password'"
          placeholder="请输入密码" @keyup.enter="handleLogin" @input="error = ''" />
        <button type="button" class="eye" :title="showPassword ? '隐藏密码' : '显示密码'"
          @click="showPassword = !showPassword">
          <svg v-if="!showPassword" viewBox="0 0 24 24" width="16" height="16" fill="none"
            stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
            <path d="M2 12 s3.6 -6.5 10 -6.5 S22 12 22 12 s-3.6 6.5 -10 6.5 S2 12 2 12Z" />
            <circle cx="12" cy="12" r="2.6" />
          </svg>
          <svg v-else viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor"
            stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
            <path d="M2 12 s3.6 -6.5 10 -6.5 S22 12 22 12 s-3.6 6.5 -10 6.5 S2 12 2 12Z" />
            <circle cx="12" cy="12" r="2.6" />
            <path d="M4 20 L20 4" />
          </svg>
        </button>
      </div>

      <!-- 校验错误提示 -->
      <p class="error" :class="{ show: error !== '' }">
        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor"
          stroke-width="2" stroke-linecap="round">
          <circle cx="12" cy="12" r="9" />
          <path d="M12 7.5 v5.5" />
          <path d="M12 16.6 v0.1" />
        </svg>
        {{ error }}
      </p>

      <!-- 登录按钮 -->
      <button class="submit" :disabled="loading" @click="handleLogin">
        {{ loading ? '登录中…' : '登 录' }}
      </button>

      <p class="hint">演示账号：admin / 123456</p>
    </div>

    <footer class="page-footer">软件工程综合实践 · 在线考试系统 · v1.0</footer>
  </div>
</template>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: linear-gradient(135deg, #1f4fd8 0%, #3b82f6 50%, #6366f1 100%);
}

/* 背景光斑 */
.blob {
  position: absolute;
  border-radius: 50%;
  background: #fff;
  pointer-events: none;
}

.blob-1 {
  width: 520px;
  height: 520px;
  top: -140px;
  right: -80px;
  opacity: 0.06;
}

.blob-2 {
  width: 600px;
  height: 600px;
  bottom: -220px;
  left: -120px;
  opacity: 0.05;
}

.blob-3 {
  width: 300px;
  height: 300px;
  bottom: 60px;
  right: 90px;
  opacity: 0.04;
}

/* 卡片 */
.login-card {
  position: relative;
  z-index: 1;
  width: 420px;
  max-width: calc(100vw - 32px);
  padding: 40px 56px 44px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 18px 60px rgba(11, 31, 92, 0.28);
  text-align: center;
}

.logo {
  width: 92px;
  height: 92px;
  margin: 0 auto 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 24px;
  background: #e8f3ff;
  color: #409eff;
}

.title {
  margin: 0;
  font-size: 27px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #1f2d3d;
}

.subtitle {
  margin: 6px 0 0;
  font-size: 12px;
  letter-spacing: 3px;
  color: #909399;
}

.divider {
  height: 1px;
  margin: 24px 0 22px;
  background: #ebeef5;
}

/* 输入框 */
.field {
  position: relative;
  display: flex;
  align-items: center;
  height: 48px;
  margin-bottom: 16px;
  padding: 0 12px;
  border: 1.5px solid #dcdfe6;
  border-radius: 8px;
  background: #fff;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.field:focus-within {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.12);
}

.field .icon {
  display: flex;
  color: #a8abb2;
  transition: color 0.2s;
}

.field:focus-within .icon,
.field.active .icon {
  color: #409eff;
}

.field input {
  flex: 1;
  height: 100%;
  min-width: 0;
  padding: 0 10px;
  border: none;
  outline: none;
  background: transparent;
  font-size: 15px;
  color: #1f2d3d;
  font-family: inherit;
}

.field input::placeholder {
  color: #c0c4cc;
}

.eye {
  display: flex;
  padding: 4px;
  border: none;
  background: transparent;
  color: #c0c4cc;
  cursor: pointer;
  transition: color 0.2s;
}

.eye:hover {
  color: #409eff;
}

/* 错误提示 */
.error {
  display: flex;
  align-items: center;
  gap: 5px;
  height: 18px;
  margin: 0 0 8px;
  font-size: 12.5px;
  color: #f56c6c;
  text-align: left;
  opacity: 0;
  transform: translateY(-3px);
  transition: opacity 0.2s, transform 0.2s;
}

.error.show {
  opacity: 1;
  transform: translateY(0);
}

/* 登录按钮 */
.submit {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(90deg, #409eff 0%, #337ecc 100%);
  color: #fff;
  font-size: 16.5px;
  font-weight: 600;
  letter-spacing: 4px;
  font-family: inherit;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(64, 158, 255, 0.3);
  transition: transform 0.15s, box-shadow 0.2s, filter 0.2s;
}

.submit:hover:not(:disabled) {
  filter: brightness(1.06);
  box-shadow: 0 6px 18px rgba(64, 158, 255, 0.4);
}

.submit:active:not(:disabled) {
  transform: translateY(1px);
}

.submit:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.hint {
  margin: 18px 0 0;
  font-size: 12px;
  color: #c0c4cc;
}

.page-footer {
  position: relative;
  z-index: 1;
  margin-top: 34px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.72);
}
</style>
