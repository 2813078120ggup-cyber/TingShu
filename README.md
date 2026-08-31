# TingShu 听书项目

基于 Java 17、Spring Boot 3.0.5、Spring Cloud 2022.0.2 和 Spring Cloud Alibaba 的听书平台后端项目。

**当前为开发初始版本，不具备直接上线条件。** 已有分类查询和文件上传接口，其余大部分业务控制器与服务仍为骨架。网关鉴权尚未实现。具体审查结论见 [代码审查记录](docs/CODE_REVIEW.md)。

接口资料：[中文接口文档](docs/api-reference.md) · [OpenAPI 定义](docs/openapi.json)。仅将已有服务端实现的接口列为可调用契约，其余控制器和外部调用单独说明。

## 项目结构

| 路径 | 内容 |
| --- | --- |
| `tingshu-parent/common` | 公共工具、服务配置、日志和 RabbitMQ 工具 |
| `tingshu-parent/model` | 实体、查询对象、VO 和校验器 |
| `tingshu-parent/server-gateway` | Spring Cloud Gateway 网关 |
| `tingshu-parent/service` | 专辑、用户、搜索、账户、评论、调度、直播、订单、支付服务 |
| `tingshu-parent/service-client` | 服务间 Feign 客户端，包括系统服务客户端 |
| `config/nacos/DEFAULT_GROUP` | 脱敏后的 Nacos 配置模板 |

本仓库暂不包含前端、数据库建表/初始化脚本或 `service-system` 服务实现。模板中保留系统服务配置和网关路由，供后续接入使用。

## 构建与测试

安装 JDK 17 和 Maven，在仓库根目录执行：

```powershell
mvn -B -ntp -f tingshu-parent/pom.xml -DskipTests=false -Dmaven.test.skip=false verify
```

部分原有模块设置了 `skipTests=true`，上述命令显式启用测试。构建成功只代表编译、打包和已存在的单元测试通过，不代表外部基础设施或业务流程可用。

## 本地配置

1. 按服务需要准备 Nacos、MySQL、Redis、RabbitMQ、MinIO、Elasticsearch、MongoDB、XXL-JOB 等依赖。
2. 参考 [Nacos 模板说明](config/nacos/README.md)，配置对应基础设施地址及凭据，再以原文件名作为 Data ID 导入 Nacos 的 `DEFAULT_GROUP`。
3. `bootstrap.properties` 和配套配置中的服务主机地址统一为 `192.168.6.129`，原有端口保持不变。可通过 `SPRING_CLOUD_NACOS_DISCOVERY_SERVER_ADDR`、`SPRING_CLOUD_NACOS_CONFIG_SERVER_ADDR` 覆盖 Nacos 地址，通过 `SPRING_PROFILES_ACTIVE` 切换环境。
4. 在应用进程环境中设置所用模板中的 `${变量名}`。模板不包含真实密码、云服务密钥或支付证书。

两个静态签名工具使用以下外部配置，缺失或空白时会拒绝签名，不再使用源码内置密钥：

| 用途 | 环境变量 | 可选 JVM 系统属性（优先） |
| --- | --- | --- |
| 订单签名 | `ORDER_SIGN_KEY` | `tingshu.order.sign-key` |
| 直播推流签名 | `LIVE_PUSH_KEY` | `tingshu.live.push-key` |

这些静态工具不会自动读取 Nacos 属性；应向对应 Java 进程注入环境变量，优先使用环境变量以免密钥进入命令行历史。直播工具的推拉流域名仍保留原示例值，接入真实直播前须调整为自己的域名。Java 不会自动加载 `.env` 文件。

支付私钥通过 `wechat.v3pay.privateKeyPath` 指定外部文件路径。支付模块已排除私钥/密钥库资源打包；请勿将私钥放入 JAR 或提交到 Git。

## 提交安全

- 原始 `tingshu-parent/资料/DEFAULT_GROUP/` 配置只保留在本机，已由 `.gitignore` 排除。
- 私钥、证书密钥库、IDE 文件、日志与 Maven 构建产物不进入版本控制。
- 首次提交前已将源码中的直播/订单签名密钥改为外部配置，并移除签名原文和签名结果日志。
- 如果原有凭据曾在其他位置共享或发布，应轮换相关密码、云服务密钥和支付私钥；本次清理不等同于凭据轮换。
