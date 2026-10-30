# 工作区定位：学校软件工程综合实践课

本工作区 project_01 是学校「软件工程综合实践」课程的专属项目工作区。
用户关于该实践课的一切问题（需求梳理、编码、调试、文档、答辩材料）都在此工作区内处理。

---

## 项目：在线考试系统（exam）

课程项目，题目为「在线考试系统」。需求来源见 docs/在线考试系统需求与操作流程文档.pdf。

### 目录结构

project_01/
├── AGENTS.md                     本文件
├── README.md                     项目说明（进度部分可能滞后，以本文件为准）
├── docs/                         课程 PDF、需求文档、每周产出文档
│   ├── 第N周-上午-软件工程综合实践教程.pdf    产出：需求/设计/文档/测试/答辩
│   ├── 第N周-下午-软件开发项目实训教程.pdf    产出：可运行代码
│   └── 第6周-*.md                第 6 周的三份产出文档
├── exam-backend01/               Spring Boot 后端
│   ├── sql/schema.sql            建表脚本（手动执行，勿放 resources）
│   └── src/{main,test}/java/com/hxf/exambackend01/
└── exam_front/                   Vue 3 + Vite 前端（PC 管理端，不是学生端）

### 技术栈与版本

| 层 | 选型 |
|---|---|
| 后端框架 | Spring Boot 3.2.5 |
| JDK | 编译目标 17，本机运行 JDK 21+ |
| 持久层 | MyBatis-Plus 3.5.7（mybatis-plus-spring-boot3-starter） |
| 数据库 | MySQL 8.x，库 hxf_exam |
| 鉴权 | Spring Security + JWT（jjwt 0.12.x），BCrypt |
| 构建 | Maven（有 wrapper），后端端口 8080 |
| 前端 | Vue 3.5 + Vite 8 + Element Plus 2.14 + Pinia 4 + vue-router 4 + axios |
| 前端端口 | 5173，/api 由 Vite 代理转发到 8080（rewrite 去掉 /api 前缀） |

### 数据库

- 建表脚本：exam-backend01/sql/schema.sql，共 11 张表，含 3 个角色与 admin 账号
- 脚本含 DROP TABLE IF EXISTS，绝不能放进 src/main/resources/，否则启动时会清空数据
- application.yml 中数据库密码已明文写入（不依赖 DB_PASSWORD 环境变量）
- admin 初始密码 123456，首次登录后由 AuthServiceImpl 自动升级为 BCrypt 密文
- question_category 表没有 (parent_id, name) 唯一索引，同级重名只在 Java 层校验，直接跑 SQL 可绕过
- question_category 物理删除；question / sys_user / paper 逻辑删除（@TableLogic）

### 当前进度（第 6 周结束）

- [x] 数据库设计与建表脚本
- [x] Spring Boot + MyBatis-Plus 骨架，冒烟测试通过
- [x] 11 张表实体映射
- [x] 统一响应体 R + 全局异常处理
- [x] 登录鉴权（JWT + BCrypt）
- [x] 用户与角色管理：7 个接口 + 用户管理页
- [x] 题库管理：9 个接口 + 分类管理页 + 题目管理页（第 6 周）
- [ ] 题库 Excel 批量导入导出（第 7 周）
- [ ] 试卷组卷（第 8 周）
- [ ] 考试与判分（第 9~12 周）
- [ ] 统计与安全加固（第 13~14 周）

### 课程进度（16 周，两门课合并）

- 上午 4 课时《软件工程综合实践》：工程过程，产出文档/图/原型
- 下午 4 课时《软件开发项目实训》：编码实战，产出可运行代码
- 路线：1~2 需求 → 3~9 设计+开发 → 10~12 学生端+判分 → 13~14 统计+安全 → 15 测试 → 16 部署+答辩
- 当前：第 6 周已完成，下一步第 7 周（题库 Excel 导入导出，需引入 EasyExcel）

### 后端接口清单

| 模块 | 接口 |
|---|---|
| 认证 | POST /auth/login |
| 用户 | GET /user/page、GET /user/roles、POST /user、PUT /user、DELETE /user/{id}、PUT /user/{id}/status/{status}、PUT /user/{id}/reset-password |
| 分类 | GET /category/tree、POST /category、PUT /category、DELETE /category/{id} |
| 题目 | GET /question/page、GET /question/{id}、POST /question、PUT /question、DELETE /question/{id} |

SecurityConfig 使用 .anyRequest().authenticated()，新接口自动受保护，无需逐个配置。
未认证访问返回 403（不是 401），因为 Spring Security 6 默认使用 Http403ForbiddenEntryPoint。

### 题库模块的关键约定（第 6 周）

| 项 | 约定 |
|---|---|
| 题型 | type 整数：1 单选 2 多选 3 判断 4 填空 5 简答；前端用字符串 SINGLE/MULTI/JUDGE/FILL/SHORT |
| 选项 | 只有选择题有值，存 JSON 数组字符串（TEXT 而非 MySQL JSON 类型），其余为 NULL |
| 答案 | 单选 "B"；多选 "A,C"（逗号分隔、保存前排序）；判断 "A" 对 / "B" 错；填空 "北京,上海"；简答整段文本 |
| 分类层级 | parent_id = 0 表示顶级，绝不能用 NULL；树由后端 buildTree 递归组装 |
| 字段名差异 | 前端 stem ↔ 数据库 content；查询参数 keyword 用于 LIKE content |
| 创建人 | creatorId 由后端从 SecurityContext 取 username 再查库，不能前端传 |
| 性能 | page() 里批量查分类名与用户名做成 Map，toVO 不再查库（避免第 5 周的 N+1） |
| 现状数据 | 分类三级：计算机网络/数据结构 各 6 与 5 个二级，共 22 个三级；题目 5 道 |

### 前端现状

| 文件 | 作用 |
|---|---|
| views/LoginPage.vue | 登录页 |
| views/Layout.vue | 后台壳：侧边栏（3 个菜单）+ 顶栏 + router-view |
| views/user/UserList.vue | 用户管理：搜索 + 表格 + 分页 + 对话框 |
| views/question/CategoryList.vue | 分类管理：el-tree + 三级增删改 |
| views/question/QuestionList.vue | 题目管理：4 筛选 + 5 种题型联动表单 + 预览 |
| utils/request.js | axios 封装：baseURL /api、自动带 token、统一解包与错误提示 |
| api/{auth,user,question}.js | 接口函数层 |

### 课程材料与仓库的已知差异

1. 课程用数据库名 exam，本仓库用 hxf_exam
2. 课程包名 com.exam，本仓库 com.hxf.exambackend01
3. 教程布局文件名 Home.vue，本仓库为 Layout.vue
4. 学生端是 Uniapp 移动端（第 10 周才做），exam_front 是 PC 管理端
5. 教程路径写 db/schema.sql，实际为 exam-backend01/sql/schema.sql

### 已知缺口

见 docs/第6周-已知缺口汇总G1-G8.md（含第 5 周遗留缺口补记）。答辩与代码走查按此表逐条核对。

### Git

- 远端：git@github.com:xiaoxue-ixue/hxf-exam-system.git（SSH，不需要走代理端口）
- 主分支：main
- docs/*.pdf 属付费课程材料，已在 .gitignore 排除

### 本机注意事项

- Windows PowerShell 5.1（不是 pwsh 7）；含中文的 .ps1 必须存为 UTF-8 with BOM
- .ps1 中不要用行首 --flag（会被当成递减运算符），改用数组 splatting
- Get-Content 读 UTF-8 中文可能乱码，判断编码用 [System.IO.File]::ReadAllBytes
- 本机无 ~/.m2/settings.xml；Maven 首次构建需要联网
- Vite 8 在受限终端里可能因 spawn EPERM 起不来，用 IDEA 自带终端或完整权限终端启动
- 工具终端的路径白名单可能仍指向改名前的旧路径（D:\Java实战项目\hxf\project_01），
  对当前路径 D:\Java Hands-on Project\hxf\project_01 执行 Write/Edit/Delete 会被拒绝；
  遇到这种情况改为输出代码或写临时目录，由用户手动复制