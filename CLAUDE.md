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

### 后端模块完成度：7/8（约 88%），83 个 Java 文件，编译零错误

| 模块 | 状态 | Controller | Service | 对应表 |
|------|------|------------|---------|--------|
| 认证 (Auth) | ✅ | AuthController | UserServiceImpl | user, role, user_role |
| 课程 & 章节 | ✅ | CourseController, ChapterController | CourseServiceImpl, ChapterServiceImpl | course, chapter, user_course_progress, user_chapter_progress |
| 靶场 (Lab) | ✅ | LabController | LabServiceImpl | lab, lab_attempt, lab_favorite |
| 测验 (Quiz) | ✅ | QuizController | QuizServiceImpl | quiz, quiz_question, quiz_record |
| 用户管理 | ✅ | UserController | UserServiceImpl | user, user_course_progress, user_chapter_progress, lab_attempt, quiz_record |
| AI 推荐 | ✅ | AiController | AiServiceImpl | ai_recommendation, ai_conversation |
| 漏洞报告 | ❌ | 未创建 | 未创建 | vulnerability_report（表已建） |
| 管理后台 | ❌ | 未创建 | 未创建 | notice, tag（表已建） |

### 已创建的文件清单

**Entity (15个):** User, Role, UserRole, Course, Chapter, UserCourseProgress, UserChapterProgress, Lab, LabAttempt, LabFavorite, Quiz, QuizQuestion, QuizRecord, AiRecommendation, AiConversation

**Mapper (15个):** 全部继承 BaseMapper<T>，零 XML 配置

**DTO (22个):** LoginRequest/Response, RegisterRequest, UserInfoResponse, UpdateProfileRequest, ChangePasswordRequest, UserStatisticsResponse, CourseListResponse, CourseDetailResponse, ChapterDetailResponse, LabListResponse, LabDetailResponse, LabSubmitRequest/Response, LabAttemptResponse, QuizDetailResponse, QuizSubmitRequest/Response, QuizRecordResponse, RecommendationResponse, ChatRequest, ChatResponse, ConversationHistoryResponse

**Service (6接口+6实现):** UserService, CourseService, ChapterService, LabService, QuizService, AiService

**Controller (7个):** AuthController, UserController, CourseController, ChapterController, LabController, QuizController, AiController

**Security:** JwtUtil (JJWT 0.12+), JwtAuthenticationFilter (OncePerRequestFilter), SecurityConfig (无状态会话)

**Common:** Result<T> (统一响应), GlobalExceptionHandler

**配置:** application.yml, init.sql (17张表 + 种子数据)

### 架构关键设计

- **三层解耦**: Controller → Service → Mapper，全部构造器注入
- **无状态 JWT 认证**：token 24h 有效期，每次请求独立验证
- **权限模型**：`/api/auth/register` `/api/auth/login` 及 GET 课程/章节/靶场公开，其余需认证
- **userId 注入**：JWT Filter 将 userId 存入 Authentication.details，SecurityUtils 统一获取，各 Controller 无需查库
- **输入校验**：DTO 使用 @Valid/@NotBlank/@Size/@Email 声明式校验，GlobalExceptionHandler 统一处理
- **密码加密**：BCryptPasswordEncoder
- **零 XML**：MyBatis Plus 纯注解 + LambdaQueryWrapper
- **AI 推荐**：基于弱项分析（漏洞类型正确率 < 60%）的规则评分算法 + 关键词匹配 AI 对话

### 已知待改进

- JWT Filter 未将 userId 注入 Authentication，导致每个方法额外查库转 username→userId
- 改密码后旧 token 仍有效（可加 token_version 字段解决）
- 异常处理只有 IllegalArgumentException(400) 和 Exception(500) 两类
- DTO 无 @Valid 声明式校验，手动 StringUtils 校验
- 前端项目尚未启动

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

## 待办清单

- [x] ~~**第5阶段：** AI 推荐模块（规则评分推荐算法 + 关键词AI对话）~~
- [ ] **第6阶段：** 漏洞报告模块 + 管理后台（notice/tag CRUD）
- [ ] **第7阶段：** Vue 3 前端项目（Vite 创建、登录/注册/首页布局）
- [ ] **第8阶段：** 前后端联调、数据填充、整体打磨
