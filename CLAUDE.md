# AI辅助的Web安全学习与漏洞靶场训练系统

## 项目概述

计算机本科毕设项目，前后端分离的 Web 安全学习平台。

- **技术栈：** Vue 3 + Spring Boot + MySQL
- **用户角色：** 学生/普通用户、管理员/教师
- **核心功能：** 课程学习、靶场练习、AI 推荐、AI 漏洞报告生成
- **开发者水平：** 编程基础较弱，需要手把手指导

## 项目文档

- `系统功能需求说明书.md` — 完整需求文档（8大模块、15张表、60+ API）
- `agents.md` — 开发者背景说明

## 当前进度（截至 2026-05-14）

### 后端：8/8 模块完成，103 个 Java 文件，编译零错误 ✅

| 模块 | 状态 | Controller | Service | 对应表 |
|------|------|------------|---------|--------|
| 认证 (Auth) | ✅ 已审查 | AuthController | UserServiceImpl | user, role, user_role |
| 课程 & 章节 | ✅ 已审查 | CourseController, ChapterController | CourseServiceImpl, ChapterServiceImpl | course, chapter, user_course_progress, user_chapter_progress |
| 靶场 (Lab) | ✅ 已审查 | LabController | LabServiceImpl | lab, lab_attempt, lab_favorite |
| 测验 (Quiz) | ✅ 已审查 | QuizController | QuizServiceImpl | quiz, quiz_question, quiz_record |
| 用户管理 | ✅ 已审查 | UserController | UserServiceImpl | user, user_course_progress, user_chapter_progress, lab_attempt, quiz_record |
| AI 推荐 | ✅ 已审查 | AiController | AiServiceImpl | ai_recommendation, ai_conversation |
| 漏洞报告 | ✅ 已审查 | ReportController | ReportServiceImpl | vulnerability_report |
| 管理后台 | ✅ 已审查 | AdminController | AdminServiceImpl | notice, tag |

### 前端：27 个文件，Vite + Vue 3，第8阶段完成 ✅

| 类别 | 文件 |
|------|------|
| 入口/配置 | App.vue, main.js, router/index.js, vite.config.js |
| 样式/状态 | styles/global.css (深色赛博朋克主题), stores/auth.js |
| API 层 | api/index.js (axios + token 拦截器 + 业务错误码检查) |
| 登录/注册 | Login.vue, Register.vue (终端风格 UI) |
| 仪表盘 | Dashboard.vue (4 统计卡片 + AI 推荐 + 快速入口) |
| 课程 | Courses.vue (筛选+进度条), CourseDetail.vue (章节列表+完成进度) |
| 靶场 | Labs.vue (筛选+状态), LabDetail.vue (Flag 提交+提示+收藏) |
| 测验 | Quiz.vue (历史记录+答题模式, radio/checkbox/input) |
| AI 助手 | AiChat.vue (对话气泡+快捷提问+打字动画) |
| 漏洞报告 | Reports.vue (列表+风险标签), ReportEdit.vue (AI 生成/手动编辑) |
| 管理后台 | admin/Notices.vue (通知 CRUD), admin/Tags.vue (标签 CRUD) |

### 后端已创建的文件

**Entity (18个):** User, Role, UserRole, Course, Chapter, UserCourseProgress, UserChapterProgress, Lab, LabAttempt, LabFavorite, Quiz, QuizQuestion, QuizRecord, AiRecommendation, AiConversation, VulnerabilityReport, Notice, Tag

**Mapper (18个):** 全部继承 BaseMapper<T>，零 XML 配置

**DTO (30个):** LoginRequest/Response, RegisterRequest, UserInfoResponse, UpdateProfileRequest, ChangePasswordRequest, UserStatisticsResponse, CourseListResponse, CourseDetailResponse, ChapterDetailResponse, LabListResponse, LabDetailResponse, LabSubmitRequest/Response, LabAttemptResponse, QuizDetailResponse, QuizSubmitRequest/Response, QuizRecordResponse, RecommendationResponse, ChatRequest, ChatResponse, ConversationHistoryResponse, ReportGenerateRequest, ReportRequest, ReportResponse, ReportListResponse, NoticeRequest, NoticeResponse, TagRequest, TagResponse

**Service (8接口+8实现):** UserService, CourseService, ChapterService, LabService, QuizService, AiService, ReportService, AdminService

**Controller (9个):** AuthController, UserController, CourseController, ChapterController, LabController, QuizController, AiController, ReportController, AdminController

**Security:** JwtUtil (JJWT 0.12+), JwtAuthenticationFilter (OncePerRequestFilter), SecurityConfig (无状态会话), SecurityUtils, LoginRateLimiter, RoleConstants

**Common:** Result<T> (统一响应), GlobalExceptionHandler

**配置:** application.yml, init.sql (17张表), seed_data.sql (联调测试数据)

### 架构关键设计

- **三层解耦**: Controller → Service → Mapper，全部构造器注入
- **无状态 JWT 认证**：token 24h 有效期，每次请求独立验证
- **权限模型**：`/api/auth/register` `/api/auth/login` 及 GET 课程/章节/靶场公开，其余需认证
- **userId 注入**：JWT Filter 将 userId 存入 Authentication.details，SecurityUtils 统一获取，各 Controller 无需查库
- **输入校验**：DTO 使用 @Valid/@NotBlank/@Size/@Email 声明式校验，GlobalExceptionHandler 统一处理
- **密码加密**：BCryptPasswordEncoder
- **零 XML**：MyBatis Plus 纯注解 + LambdaQueryWrapper
- **AI 推荐**：基于弱项分析（漏洞类型正确率 < 60%）的规则评分算法 + 关键词匹配 AI 对话
- **AI 漏洞报告**：模板化生成结构化漏洞报告（含复现步骤、影响分析、修复建议），支持手动创建/编辑
- **管理后台**：通知/标签 CRUD + 管理员角色校验，GET 公开/写操作需 ADMIN 权限
- **前端深色主题**：全局 CSS 变量覆盖 Element Plus 80+ 属性，Vite proxy `/api` → `http://localhost:8080`
- **前端路由守卫**：router.beforeEach 根据 token 存在性自动跳转登录页或仪表盘
- **前端认证**：axios 请求拦截器自动注入 Bearer token，401 响应自动清除 token 并跳转登录

### 已知待改进

- [x] ~~JWT Filter 未将 userId 注入 Authentication~~ → 已修复：userId 存入 Authentication.details，SecurityUtils 统一获取
- [x] ~~异常处理只有两类~~ → 已新增 MethodArgumentNotValidException 处理 + 隐藏内部错误消息
- [x] ~~DTO 无声明式校验~~ → 已添加 @Valid/@NotBlank/@Size/@Email
- [ ] 改密码后旧 token 仍有效（可加 token_version 字段解决，涉及 DB 改动暂缓）

### 认证模块代码审查结果（2026-05-12）

**🔴 高危 (3项):**

| 问题 | 位置 | 说明 |
|------|------|------|
| CORS 配置违规 | CorsConfig.java:17 | `allowedOriginPatterns("*")` + `allowCredentials(true)` 浏览器会拒绝，前端联调时直接失败 |
| JWT userId 未利用 | JwtAuthenticationFilter.java:37-40 | claims 中有 userId 但 Authentication 只存了 username，其余模块被迫查库转换 |
| 异常消息泄露 | GlobalExceptionHandler.java:15-16 | `Exception.getMessage()` 可能暴露 SQL 错误等内部信息给前端 |

**🟡 中危 (4项):**

| 问题 | 位置 | 说明 |
|------|------|------|
| 旧 token 不失效 | UserServiceImpl.java:159 | 改密码后旧 JWT 仍然有效 |
| 登录无频率限制 | AuthController.java:32 | 无暴力破解防护 |
| 禁用用户可查 /me | UserServiceImpl.java:122 | getCurrentUser() 缺少 status 检查 |
| DTO 无声明式校验 | 全部 DTO | 缺 @NotBlank/@Size/@Email，手动 StringUtils 校验 |

**🟢 低危/代码质量 (6项):**

| 问题 | 说明 |
|------|------|
| 错误消息中英混用 | "用户名或密码错误"(中) vs "user is disabled"(英) vs "user not found"(英) |
| 邮箱未校验唯一性 | 多个用户可注册同一邮箱 |
| 密码无强度要求 | 接受任意非空密码 |
| 无日志记录 | 整个认证链路无 log 输出 |
| UserServiceImpl 职责过重 | 认证+用户资料+统计混在一起，建议拆 AuthServiceImpl |
| 硬编码角色码 | `"USER"` 字符串散落在 register() 中，应定义常量

### 环境信息

- Java 21 + Maven 3.8.5 + Node.js v24.15
- MySQL 8.0.18 Docker (容器名 `mysql`, 端口 3306, 密码 `123456`)
- 本机 MySQL80 服务已停止（避免端口冲突）

### 第8阶段联调修复记录（2026-05-14）

| 问题 | 根因 | 修复 |
|------|------|------|
| 中文乱码（课程名、报告） | MySQL connection charset 为 latin1 | 重建 DB 时指定 `--default-character-set=utf8mb4` |
| 管理后台操作无反应 | catch 块静默吞错误 + 前端无 roles 信息 | 补 ElMessage.error + LoginResponse/前端 store 加 roles |
| 注册 CORS 403 | CORS 配置只允许固定端口，dev server 换了端口 | CorsConfig 改为 `http://localhost:*` 通配符端口 |
| 登录无反应 | 后端业务错误走 HTTP 200（Result 模式），axios 拦截器未检查 code | 拦截器加 `data.code !== 200` 时 reject，Login.vue 加 catch |
| 测验提交 500 | QuizSubmitRequest 的 answers 类型 Map<Long, String> 不支持多选题数组 | 改为 Map<Long, Object> |
| 测验判分错误 | MySQL JSON 列返回 `"B"`（带引号字符串），判等失败 | checkAnswer 用 Jackson 解析 JSON 字符串 |
| 测验选项值不匹配 | 前端发完整选项文本 "B. 使用预编译语句"，后端存字母 "B" | Quiz.vue 用 `opt.charAt(0)` 取值 |

### 种子数据

- `backend/.../resources/seed_data.sql` — 包含用户（admin/student）、课程（SQL注入/XSS/CSRF各含3章）、靶场（6个含难度/类型/CVE）、测验（3套含10题）、通知（3条）

## 待办清单

- [x] ~~**第5阶段：** AI 推荐模块（规则评分推荐算法 + 关键词AI对话）~~
- [x] ~~**第6阶段：** 漏洞报告模块 + 管理后台（notice/tag CRUD）~~
- [x] ~~**第7阶段：** Vue 3 前端项目（Vite 创建、登录/注册/首页布局）~~
- [x] ~~**第8阶段：** 前后端联调、数据填充、整体打磨~~
