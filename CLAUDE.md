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

## 当前进度（截至 2026-05-12）

### 后端模块完成度：6/8（约 75%），68 个 Java 文件，编译零错误

| 模块 | 状态 | Controller | Service | 对应表 |
|------|------|------------|---------|--------|
| 认证 (Auth) | ✅ | AuthController | UserServiceImpl | user, role, user_role |
| 课程 & 章节 | ✅ | CourseController, ChapterController | CourseServiceImpl, ChapterServiceImpl | course, chapter, user_course_progress, user_chapter_progress |
| 靶场 (Lab) | ✅ | LabController | LabServiceImpl | lab, lab_attempt, lab_favorite |
| 测验 (Quiz) | ✅ | QuizController | QuizServiceImpl | quiz, quiz_question, quiz_record |
| 用户管理 | ✅ | UserController | UserServiceImpl | user, user_course_progress, user_chapter_progress, lab_attempt, quiz_record |
| AI 推荐 | ❌ | 未创建 | 未创建 | ai_recommendation, ai_conversation（表已建） |
| 漏洞报告 | ❌ | 未创建 | 未创建 | vulnerability_report（表已建） |
| 管理后台 | ❌ | 未创建 | 未创建 | notice, tag（表已建） |

### 已创建的文件清单

**Entity (13个):** User, Role, UserRole, Course, Chapter, UserCourseProgress, UserChapterProgress, Lab, LabAttempt, LabFavorite, Quiz, QuizQuestion, QuizRecord

**Mapper (13个):** 全部继承 BaseMapper<T>，零 XML 配置

**DTO (14个):** LoginRequest/Response, RegisterRequest, UserInfoResponse, UpdateProfileRequest, ChangePasswordRequest, UserStatisticsResponse, CourseListResponse, CourseDetailResponse, ChapterDetailResponse, LabListResponse, LabDetailResponse, LabSubmitRequest/Response, LabAttemptResponse, QuizDetailResponse, QuizSubmitRequest/Response, QuizRecordResponse

**Service (5接口+5实现):** UserService, CourseService, ChapterService, LabService, QuizService

**Controller (6个):** AuthController, UserController, CourseController, ChapterController, LabController, QuizController

**Security:** JwtUtil (JJWT 0.12+), JwtAuthenticationFilter (OncePerRequestFilter), SecurityConfig (无状态会话)

**Common:** Result<T> (统一响应), GlobalExceptionHandler

**配置:** application.yml, init.sql (17张表 + 种子数据)

### 架构关键设计

- **三层解耦**: Controller → Service → Mapper，全部构造器注入
- **无状态 JWT 认证**：token 24h 有效期，每次请求独立验证
- **权限模型**：`/api/auth/register` `/api/auth/login` 及 GET 课程/章节/靶场公开，其余需认证
- **双重认证**：`resolveUserId`（可选登录）用于公开接口，`requireUserId`（强制登录）用于写操作
- **密码加密**：BCryptPasswordEncoder
- **零 XML**：MyBatis Plus 纯注解 + LambdaQueryWrapper

### 已知待改进

- JWT Filter 未将 userId 注入 Authentication，导致每个方法额外查库转 username→userId
- 改密码后旧 token 仍有效（可加 token_version 字段解决）
- 异常处理只有 IllegalArgumentException(400) 和 Exception(500) 两类
- DTO 无 @Valid 声明式校验，手动 StringUtils 校验
- 前端项目尚未启动

### 环境信息

- Java 21 + Maven 3.8.5 + Node.js v24.15
- MySQL 8.0.18 Docker (容器名 `mysql`, 端口 3306, 密码 `123456`)
- 本机 MySQL80 服务已停止（避免端口冲突）

## 待办清单

- [ ] **第5阶段：** AI 推荐模块（AI 推荐算法、对接大模型 API、ai_conversation 记录）
- [ ] **第6阶段：** 漏洞报告模块 + 管理后台（notice/tag CRUD）
- [ ] **第7阶段：** Vue 3 前端项目（Vite 创建、登录/注册/首页布局）
- [ ] **第8阶段：** 前后端联调、数据填充、整体打磨
