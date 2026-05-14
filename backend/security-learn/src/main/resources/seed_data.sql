-- ============================================
-- 补充种子数据（用户需要在后端启动后通过 API 注册）
-- ============================================

-- 课题：补充课程
INSERT IGNORE INTO course (id, title, description, difficulty, status, sort_order) VALUES
(3, 'CSRF攻击与防护', '学习跨站请求伪造的原理、攻击场景和防护方案。', 1, 1, 3),
(4, '文件上传漏洞', '了解文件上传漏洞的利用方式和安全检查方法。', 2, 1, 4),
(5, '命令注入与代码执行', '掌握命令注入和远程代码执行的原理与防御。', 2, 1, 5),
(6, 'SSRF服务端请求伪造', '学习SSRF漏洞的产生原因和利用技巧。', 3, 1, 6);

-- 章节：补充章节
INSERT IGNORE INTO chapter (id, course_id, title, content, sort_order) VALUES
(4, 1, 'SQL注入进阶技术', '联合查询、报错注入、盲注等高级注入技术。', 3),
(5, 1, 'SQL注入防御实践', '使用预编译语句、WAF绕过防护和白名单验证。', 4),
(6, 2, 'XSS存储型漏洞', '恶意脚本被永久存储在服务器端并在用户访问时执行。', 2),
(7, 2, 'XSS防御方案', '输出编码、CSP策略和HttpOnly Cookie的使用。', 3),
(8, 3, 'CSRF原理与示例', '攻击者利用用户的登录状态发起非预期请求。', 1),
(9, 3, 'CSRF防护策略', 'Token验证、SameSite Cookie和Referer检查。', 2),
(10, 4, '文件上传漏洞原理', '未经验证的文件上传可导致任意文件写入和远程代码执行。', 1),
(11, 4, '文件上传绕过技巧', 'Content-Type绕过、扩展名绕过、图片马等。', 2),
(12, 5, '命令注入基础', '系统命令拼接导致的任意命令执行漏洞。', 1),
(13, 6, 'SSRF漏洞原理', '服务端请求未做限制导致内网资源访问。', 1);

-- 靶场：补充靶场
INSERT IGNORE INTO lab (id, title, description, vulnerability_type, difficulty, flag, hint, status) VALUES
(3, 'SQL注入-联合查询', '使用联合查询技术获取数据库中的隐藏数据。', 'SQL_INJECTION', 2, 'flag{sql_union}', '尝试使用 UNION SELECT 构造联合查询。', 1),
(4, 'SQL注入-盲注', '在无回显的情况下通过布尔或时间盲注获取数据。', 'SQL_INJECTION', 3, 'flag{sql_blind}', '观察页面响应差异或响应时间。', 1),
(5, 'XSS存储型漏洞', '在评论区提交恶意脚本，使其在管理员访问时执行。', 'XSS', 2, 'flag{xss_stored}', '存储型XSS不需要诱导受害者点击链接。', 1),
(6, 'CSRF修改密码', '构造恶意页面，利用用户登录状态修改其密码。', 'CSRF', 2, 'flag{csrf_pwd}', '构造一个自动提交表单的页面。', 1),
(7, '文件上传-图片马', '上传包含恶意代码的图片文件获取服务器权限。', 'FILE_UPLOAD', 2, 'flag{upload_shell}', '尝试修改文件内容但不修改扩展名。', 1),
(8, '命令注入-Ping测试', '通过Ping功能执行系统命令。', 'COMMAND_INJECTION', 1, 'flag{cmd_inject}', '尝试在IP参数后追加系统命令。', 1);

-- 测验：补充测验及题目
INSERT IGNORE INTO quiz (id, course_id, chapter_id, title) VALUES
(1, 1, 1, 'SQL注入基础测验'),
(2, 2, 3, 'XSS基础测验'),
(3, 3, 8, 'CSRF原理测验');

INSERT IGNORE INTO quiz_question (id, quiz_id, question_type, content, options_json, answer_json, analysis, score) VALUES
(1, 1, 1, '以下哪种方式可以有效防止SQL注入？', '["A. 拼接SQL字符串", "B. 使用预编译语句", "C. 关闭数据库错误提示", "D. 使用HTTP POST方法"]', '"B"', '预编译语句将SQL结构与数据分离，是防止SQL注入最有效的方式。', 10),
(2, 1, 1, 'SQL注入攻击中，注释符"-- "的作用是什么？', '["A. 提高查询速度", "B. 注释掉后续SQL语句", "C. 加密查询内容", "D. 连接两个查询"]', '"B"', '注入时使用注释符可以忽略原始查询的后续部分，使构造的语句完整执行。', 10),
(3, 1, 2, '以下哪些SQL注入类型存在？(多选)', '["A. 联合查询注入", "B. 布尔盲注", "C. 时间盲注", "D. 堆叠查询注入"]', '["A","B","C","D"]', '以上四种都是常见的SQL注入类型。', 10),
(4, 2, 1, 'XSS攻击中，以下哪个是反射型XSS的特征？', '["A. 恶意代码存储在服务器", "B. 恶意代码在URL中且立即执行", "C. 利用数据库存储恶意负载", "D. 需要管理员审核才能触发"]', '"B"', '反射型XSS的恶意负载通常在URL中，服务端立即反射回响应页面执行。', 10),
(5, 2, 1, '以下哪个HTTP响应头可以防御XSS？', '["A. X-Content-Type-Options", "B. Content-Security-Policy", "C. Strict-Transport-Security", "D. X-Frame-Options"]', '"B"', 'CSP（Content-Security-Policy）可以限制页面加载的资源，有效防御XSS。', 10),
(6, 2, 1, '存储型XSS与反射型XSS的主要区别是？(多选)', '["A. 存储型需要服务器存储恶意代码", "B. 反射型不需要用户交互", "C. 存储型影响范围更广", "D. 反射型的危害更大"]', '["A","C"]', '存储型XSS将恶意代码持久化存储在服务器端，所有访问该页面的用户都可能受影响。', 10),
(7, 3, 1, 'CSRF攻击利用的是哪个关键因素？', '["A. 用户的输入未过滤", "B. 用户的登录Cookie自动发送", "C. 服务器版本过旧", "D. 使用了HTTPS协议"]', '"B"', 'CSRF的核心原理是浏览器会自动在请求中附带目标站点的Cookie，包括登录态Cookie。', 10),
(8, 3, 1, '以下哪些可以有效防御CSRF？(多选)', '["A. 添加CSRF Token", "B. 使用SameSite Cookie", "C. 验证Referer字段", "D. 使用POST请求代替GET"]', '["A","B","C"]', 'CSRF Token、SameSite属性和Referer验证都是有效的CSRF防御手段。', 10);

-- 标签
INSERT IGNORE INTO tag (id, tag_name, tag_type) VALUES
(1, 'SQL注入', 'vulnerability'),
(2, 'XSS', 'vulnerability'),
(3, 'CSRF', 'vulnerability'),
(4, '文件上传', 'vulnerability'),
(5, '命令注入', 'vulnerability'),
(6, 'SSRF', 'vulnerability'),
(7, '入门', 'difficulty'),
(8, '进阶', 'difficulty'),
(9, '高手', 'difficulty');

-- 通知公告
INSERT IGNORE INTO notice (id, title, content, status) VALUES
(1, '平台正式上线', '欢迎来到网络安全学习平台！本平台提供从入门到进阶的网络安全课程体系和实战靶场。', 1),
(2, '新增SQL注入进阶课程', 'SQL注入进阶课程已上线，包含联合查询、盲注等高级技术。', 1),
(3, '靶场挑战赛预告', '平台将在下月举办网络安全靶场挑战赛，敬请期待。', 0);
