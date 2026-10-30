<template>
  <el-container class="layout">
    <!-- 侧边栏 -->
    <el-aside width="200px" class="aside">
      <div class="logo">在线考试系统</div>
      <el-menu
          router
          :default-active="$route.path"
          background-color="#001529"
          text-color="#ccc"
          active-text-color="#409eff"
      >
        <el-menu-item index="/home/user">
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/home/category">
          <span>题目分类</span>
        </el-menu-item>
        <el-menu-item index="/home/question">
          <span>题目管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶栏 -->
      <el-header class="header">
        <span class="title">在线考试系统</span>
        <el-dropdown @command="handleCommand">
          <span class="user-name">
            {{ userStore.userInfo?.realName || userStore.userInfo?.username }}
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <!-- 内容区：由子路由渲染 -->
      <el-main class="main">
        <router-view/>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import {useRouter} from 'vue-router'
import {useUserStore} from '@/stores/user.js'

const router = useRouter()
const userStore = useUserStore()

function handleCommand(command) {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout {
  height: 100vh;
}

.aside {
  background: #001529;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #eee;
  background: #fff;
}

.title {
  font-size: 16px;
  font-weight: bold;
}

.user-name {
  cursor: pointer;
  color: #333;
}

.main {
  background: #f5f5f5;
}
</style>