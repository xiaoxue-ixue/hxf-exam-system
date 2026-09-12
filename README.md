# 在线考试系统（exam）

软件工程综合实践项目。当前处于**起步阶段**：后端骨架已搭好并可运行，业务代码待实现。

## 技术栈

| 层 | 选型 |
|---|---|
| 框架 | Spring Boot 4.1.1 |
| JDK | Java 17 |
| 持久层 | MyBatis-Plus 3.5.17（`mybatis-plus-spring-boot4-starter`） |
| 数据库 | MySQL 8.x |
| 构建 | Maven（含 wrapper） |
| 工具 | Lombok |

## 目录结构

```
project_01/
├── docs/                     需求与课程文档（PDF）
├── exam-backend01/           后端服务
│   ├── sql/schema.sql        建表脚本（手动执行，不在 classpath 上）
│   └── src/main/java/com/hxf/exambackend01/
│       ├── common/           统一响应体、业务异常、常量        （待实现）
│       ├── config/           MP 分页、WebMvc、跨域等配置       （待实现）
│       ├── controller/       HTTP 接口层
│       ├── service/impl/     业务逻辑层                        （待实现）
│       ├── mapper/           MyBatis-Plus Mapper
│       ├── entity/           数据库实体（11 张表已全部映射）
│       ├── dto/              请求/响应对象                     （待实现）
│       └── util/             通用工具类                        （待实现）
└── frontend/                 前端（未创建）
```

## 快速开始

1. **建库**

   在 MySQL 客户端执行 `exam-backend01/sql/schema.sql`。脚本会创建 `hxf_exam` 库、11 张表，并写入 `sys_role` 基础角色与 `admin` 账号。

   > 脚本含 `DROP TABLE IF EXISTS`，**请勿**放进 `src/main/resources/`，否则 Spring Boot 启动时会自动执行并清空数据。

2. **改配置**

   `exam-backend01/src/main/resources/application.yml` 中的数据库账号密码需改为你本地的值。

3. **启动**

   ```bash
   cd exam-backend01
   ./mvnw spring-boot:run
   ```

   访问 `http://localhost:8080/hello` 应返回「Hello, 在线考试系统后端启动成功!」。

4. **跑测试**

   ```bash
   ./mvnw test
   ```

   `MyBatisPlusSmokeTest` 会验证数据源连通、Mapper 扫描、`@TableLogic` 逻辑删除是否生效——这三项依赖第 1 步的库已建好。

## 数据模型

11 张表：`sys_user` / `sys_role` / `sys_user_role`（权限）、`question` / `question_category`（题库）、`paper` / `paper_question`（试卷）、`exam` / `exam_student`（考试）、`answer_record` / `exam_result`（答题与成绩）。

已知待处理：`@TableLogic` 逻辑删除目前只覆盖 `sys_user` / `paper` / `question` 三张表，其余 8 张无 `deleted` 字段，删除策略需统一。

## 进度

- [x] 数据库设计与建表脚本
- [x] Spring Boot + MyBatis-Plus 骨架，冒烟测试通过
- [x] 11 张表实体映射
- [ ] 补全 Mapper 与 Service 层
- [ ] 统一响应体与全局异常处理
- [ ] 登录鉴权（JWT）
- [ ] 题库 / 试卷 / 考试 / 阅卷 业务接口
- [ ] 前端
