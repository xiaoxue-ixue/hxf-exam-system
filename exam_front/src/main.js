import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import App from './App.vue'
import router from './router'

// 第 3 周（下午）教材：main.js 用变量式写法依次注册 Pinia、Router、Element Plus。
// 全量引入 Element Plus 简单直接，适合教学；生产优化可用按需引入（后续周次再讲）。
const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.mount('#app')
