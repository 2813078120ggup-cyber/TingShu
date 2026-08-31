# 初始化代码审查

审查日期：2026-08-31。范围：当前工作区的 Maven 模块、266 个原有 Java 源文件、配置与待提交文件。此次审查重点为首次发布安全、现有接口边界和构建可重复性，不是完整渗透测试或全量依赖漏洞审计。

## 首次提交前已处理

| 问题 | 处理 |
| --- | --- |
| 原始 Nacos 导出含数据库密码、云服务密钥及支付配置；支付资源目录含私钥 | 原文件保留在本机并加入忽略规则；仅提交 `config/nacos/DEFAULT_GROUP/` 的脱敏模板 |
| 直播工具和订单签名工具存在内嵌签名密钥 | 使用外部环境变量/JVM 属性；未配置或空白时拒绝签名；原 Java 文件备份在工作区外 |
| 订单签名工具记录含密钥的签名原文及签名结果 | 删除相关日志，并加入日志不泄密的回归测试 |
| 本地支付私钥可能随默认 resources 处理进入 JAR | 支付模块显式排除 `.pem`、`.key`、`.p12`、`.pfx`、`.jks`、`.keystore` 资源 |
| 模板中的 RabbitMQ 手动确认配置名拼写错误 | 仅在发布模板中改为 `acknowledge-mode`，未修改本地导出 |

上述清理防止本次推送泄露已识别的凭据，不能确认这些凭据在其他位置是否曾泄露。若原密钥已被共享或公开，应轮换。

## 仍待处理的问题

### P1：网关没有实际鉴权，文件上传可匿名调用

证据：[AuthGlobalFilter.java](../tingshu-parent/server-gateway/src/main/java/com/atguigu/tingshu/gateway/filter/AuthGlobalFilter.java) 的 `filter` 直接调用 `chain.filter(exchange)`；[FileUploadApiController.java](../tingshu-parent/service/service-album/src/main/java/com/atguigu/tingshu/album/api/FileUploadApiController.java) 暴露 `POST /api/album/fileUpload`，没有用户校验。

在未由外部网络策略保护的情况下，调用者可消耗对象存储资源。网关目前也未限制管理路径和内部接口路径。应先确定登录机制、公开接口白名单和服务间信任边界，再实现鉴权；本次初始化不擅自设计业务权限。

### P1：Fastjson 依赖处于已知风险版本范围

证据：[父 POM](../tingshu-parent/pom.xml) 固定 `fastjson.version=1.2.29`；公共消息工具和直播消息接收器存在 `JSON.parseObject` 调用。[Fastjson 官方安全公告](https://github.com/alibaba/fastjson/wiki/security_update_20220523) 说明，在特定依赖条件下，1.2.80 及以下存在反序列化风险。

此处确认版本落在公告范围内，未证明本项目具备具体可利用链。应安排依赖升级与序列化兼容性验证；本次不直接替换整套依赖，也不将历史修复版视为当前生产推荐版本。

### P2：上传接口缺少文件校验并且未显式关闭输入流

证据：[FileUploadServiceImpl.java](../tingshu-parent/service/service-album/src/main/java/com/atguigu/tingshu/album/service/impl/FileUploadServiceImpl.java) 直接使用 `file.getOriginalFilename()`、客户端提供的 Content-Type 和 `file.getInputStream()`；未检查空文件、允许类型或使用 try-with-resources。模板虽有限制请求大小，但不能替代内容校验。上线前应明确文件类型策略和对象访问策略，并验证失败时资源释放。

### P2：付费专辑缺少价格和价格类型仍能通过自定义校验

证据：[AlbumInfoVo.java](../tingshu-parent/model/src/main/java/com/atguigu/tingshu/vo/album/AlbumInfoVo.java) 将付费类型和价格/价格类型拼接后交给 [NotEmptyPaidValidator.java](../tingshu-parent/model/src/main/java/com/atguigu/tingshu/validation/NotEmptyPaidValidator.java)。当 `payType=0103` 且价格或价格类型未填写时，拼接结果为 `0103_null`，校验器仅判断拆分后长度等于 2，因此错误地返回 true。

已通过独立 Java 探针调用实际编译类复现：缺少付费价格、缺少价格类型两种输入均通过自定义校验。保存专辑接口尚未实现，目前不能据此断言已发生错误入库；接入保存流程前应拒绝空值及字符串 `null`，并校验合法价格类型和价格范围。

## 实现边界

- 当前业务控制器只有两个带 HTTP 方法映射的接口：分类查询 `GET /api/album/category/getBaseCategoryList` 和文件上传 `POST /api/album/fileUpload`。
- 登录、订单、支付、账户、搜索等控制器大部分仍是空骨架，不能把模块存在或 JAR 构建成功视为功能完成。
- 仓库没有前端、建表/初始化 SQL 或 `service-system` 服务实现，未进行网关/数据库/对象存储/支付的运行时联调。
- 原代码存在行尾空格、文件末尾空行、过时 API 和未检查类型使用；本次保留原有格式，避免大面积无关改动。

## 验证记录

- 使用 JDK 17.0.20、Maven 3.9.12，在由 Git 暂存区导出的独立目录执行 `mvn -B -ntp -f tingshu-parent/pom.xml -DskipTests=false -Dmaven.test.skip=false verify`：全部 25 个模块成功，未借用工作区旧编译产物。
- 新增的订单签名和直播签名回归测试共 4 项，全部通过，0 失败、0 错误、0 跳过。覆盖外部密钥参与签名、空白密钥拒绝和签名日志不泄密；没有进行服务运行时联调。
- 支付 JAR 中私钥/密钥库条目为 0。额外在独立副本放置 6 类不含密钥材料的测试资源，执行 Maven resources 处理后，6 个测试文件均未进入 `target/classes`。
- 11 份发布 YAML 模板及 41 份源码 XML 文件解析通过；文档相对链接检查通过。
- 对发布文件扫描私钥标记、常见令牌格式和原配置/源码中提取的 15 个凭据候选值，确认没有真实凭据残留。原有 `MD5.main` 的演示输入与一个弱密码候选重复，已人工确认是演示代码，未将其误报为凭据泄露。该扫描不能保证发现所有未知格式的秘密。
- 新增文件的 `git diff --cached --check` 通过。完整首次提交检查仍报告原代码中的 78 处空白格式问题，本次未批量格式化；编译存在原有过时 API/未检查类型警告，无编译错误。
- 原本地配置及支付私钥的 SHA-256 校验保持不变，原文件均保留；现有文件仅修改两处签名工具和支付模块 POM，其余为新增忽略规则、模板、说明文档及测试。
