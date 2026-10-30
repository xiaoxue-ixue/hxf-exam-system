import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', name: 'Login', component: () => import('../views/LoginPage.vue') },
    {
        path: '/home',
        name: 'Home',
        component: () => import('../views/Layout.vue'),
        redirect: '/home/user',
        children: [
            { path: 'user', name: 'UserList', component: () => import('../views/user/UserList.vue') },
            { path: 'category', name: 'CategoryList', component: () => import('../views/question/CategoryList.vue') },
            { path: 'question', name: 'QuestionList', component: () => import('../views/question/QuestionList.vue') },
        ],
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    if (to.path !== '/login' && !token) next('/login')
    else next()
})

export default router