<script setup>
defineProps({
  username: {
    type: String,
    default: 'admin',
  },
})

const emit = defineEmits(['logout'])

const navs = ['首页', '用户管理', '角色管理']

// 内容区占位表格：第一行用 schema.sql 里的真实种子数据
const users = [{ id: 1, username: 'admin', realName: '系统管理员', role: '管理员', status: '启用' }]
const placeholders = [1, 2, 3]
</script>

<template>
  <div class="home-page">
    <!-- 顶栏 -->
    <header class="topbar">
      <div class="brand">
        <span class="brand-logo">
          <svg viewBox="0 0 48 48" width="20" height="20" fill="none" stroke="currentColor"
            stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round">
            <path d="M24 8 L40 16 L24 24 L8 16 Z" />
            <path d="M14 20 v9 c0 0 5 5 10 5 s10 -5 10 -5 v-9" />
          </svg>
        </span>
        <span class="brand-name">在线考试系统</span>
      </div>

      <nav class="nav">
        <span v-for="(n, i) in navs" :key="n" class="nav-item" :class="{ active: i === 0 }">{{ n }}</span>
      </nav>

      <div class="user">
        <span class="avatar">
          <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor"
            stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="8" r="4" />
            <path d="M4 21 c0 -4.4 3.6 -7 8 -7 s8 2.6 8 7" />
          </svg>
        </span>
        <span class="user-name">{{ username }}</span>
        <button class="logout" @click="emit('logout')">退出</button>
      </div>
    </header>

    <!-- 主内容 -->
    <main class="content">
      <p class="breadcrumb">首页 / 工作台</p>
      <h2 class="page-title">工作台</h2>
      <p class="page-sub">在线考试系统 · 管理员端</p>

      <!-- 数据卡片 -->
      <div class="stats">
        <div class="stat" style="--bar: #409eff">
          <p class="stat-label">用户总数</p>
          <p class="stat-value">1 <small>个</small></p>
        </div>
        <div class="stat" style="--bar: #67c23a">
          <p class="stat-label">角色数量</p>
          <p class="stat-value">3 <small>管理员 / 教师 / 学生</small></p>
        </div>
        <div class="stat" style="--bar: #e6a23c">
          <p class="stat-label">当前登录</p>
          <p class="stat-value">{{ username }}</p>
        </div>
        <div class="stat" style="--bar: #909399">
          <p class="stat-label">系统版本</p>
          <p class="stat-value">v1.0</p>
        </div>
      </div>

      <!-- 内容区 -->
      <section class="panel">
        <div class="panel-head">
          <h3>用户列表</h3>
          <p>第 3 周为静态占位，第 5 周接入后端接口后填充真实数据</p>
        </div>

        <table class="table">
          <thead>
            <tr>
              <th style="width: 90px">ID</th>
              <th style="width: 200px">账号</th>
              <th style="width: 240px">姓名</th>
              <th style="width: 180px">角色</th>
              <th style="width: 140px">状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in users" :key="u.id">
              <td>{{ u.id }}</td>
              <td>{{ u.username }}</td>
              <td>{{ u.realName }}</td>
              <td><span class="tag tag-blue">{{ u.role }}</span></td>
              <td><span class="tag tag-green">{{ u.status }}</span></td>
              <td><span class="link">编辑</span></td>
            </tr>
            <tr v-for="p in placeholders" :key="'p' + p" class="skeleton-row">
              <td><span class="sk" style="width: 40px"></span></td>
              <td><span class="sk" style="width: 90px"></span></td>
              <td><span class="sk" style="width: 110px"></span></td>
              <td><span class="sk sk-tag"></span></td>
              <td><span class="sk sk-tag"></span></td>
              <td><span class="sk" style="width: 60px"></span></td>
            </tr>
          </tbody>
        </table>

        <p class="pager">共 1 条　‹ 1 ›</p>
      </section>
    </main>
  </div>
</template>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #f5f7fa;
}

/* 顶栏 */
.topbar {
  display: flex;
  align-items: center;
  height: 60px;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #409eff, #337ecc);
  color: #fff;
}

.brand-name {
  font-size: 17px;
  font-weight: 700;
  color: #1f2d3d;
}

.nav {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 56px;
}

.nav-item {
  padding: 5px 16px;
  border-radius: 6px;
  font-size: 15px;
  color: #606266;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}

.nav-item:hover {
  color: #409eff;
}

.nav-item.active {
  background: #ecf5ff;
  color: #409eff;
  font-weight: 600;
}

.user {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
  font-size: 14px;
  color: #606266;
}

.avatar {
  display: flex;
  color: #909399;
}

.logout {
  margin-left: 6px;
  padding: 5px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: #fff;
  font-size: 13px;
  font-family: inherit;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
}

.logout:hover {
  border-color: #f56c6c;
  color: #f56c6c;
}

/* 内容 */
.content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}

.breadcrumb {
  margin: 0 0 14px;
  font-size: 13px;
  color: #909399;
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #1f2d3d;
}

.page-sub {
  margin: 4px 0 22px;
  font-size: 13px;
  color: #909399;
}

/* 数据卡片 */
.stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 22px;
}

@media (max-width: 1100px) {
  .stats {
    grid-template-columns: repeat(2, 1fr);
  }
}

.stat {
  position: relative;
  overflow: hidden;
  padding: 20px 24px;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 4px 12px rgba(11, 31, 92, 0.06);
}

.stat::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 4px;
  height: 100%;
  background: var(--bar);
}

.stat-label {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.stat-value {
  margin: 8px 0 0;
  font-size: 30px;
  font-weight: 700;
  color: #1f2d3d;
  line-height: 1.2;
}

.stat-value small {
  font-size: 12.5px;
  font-weight: 400;
  color: #909399;
  margin-left: 6px;
}

/* 面板 */
.panel {
  padding: 20px 24px 16px;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 4px 12px rgba(11, 31, 92, 0.06);
}

.panel-head {
  display: flex;
  align-items: baseline;
  gap: 12px;
  padding-bottom: 14px;
  border-bottom: 1px solid #ebeef5;
}

.panel-head h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2d3d;
}

.panel-head p {
  margin: 0;
  font-size: 12px;
  color: #c0c4cc;
}

/* 表格 */
.table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 4px;
}

.table th {
  padding: 13px 12px;
  background: #fafafa;
  font-size: 13px;
  font-weight: 600;
  color: #909399;
  text-align: left;
}

.table th:first-child {
  border-radius: 6px 0 0 6px;
}

.table th:last-child {
  border-radius: 0 6px 6px 0;
}

.table td {
  padding: 14px 12px;
  border-bottom: 1px solid #ebeef5;
  font-size: 13.5px;
  color: #606266;
}

.skeleton-row td {
  padding: 18px 12px;
}

.sk {
  display: inline-block;
  height: 12px;
  border-radius: 6px;
  background: #f0f2f5;
}

.sk-tag {
  width: 52px;
  height: 22px;
  border-radius: 4px;
}

.tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.tag-blue {
  background: #ecf5ff;
  color: #409eff;
}

.tag-green {
  background: #f0f9eb;
  color: #67c23a;
}

.link {
  color: #409eff;
  font-size: 12.5px;
  cursor: pointer;
}

.link:hover {
  text-decoration: underline;
}

.pager {
  margin: 16px 0 0;
  font-size: 12.5px;
  color: #c0c4cc;
  text-align: right;
}
</style>
