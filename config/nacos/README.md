# Nacos 配置模板

`DEFAULT_GROUP/` 下的 11 个 YAML 文件由本地配置脱敏生成，保留服务端口、路由、数据库名称和主要配置结构。它们是接入参考，不是可直接用于生产的配置。

- 文件名即 Nacos Data ID，例如 `service-album-dev.yaml`；默认 Group 为 `DEFAULT_GROUP`。
- 数据库、缓存、消息队列、搜索、对象存储、Sentinel 和调度中心的主机地址统一为 `192.168.6.129`，原有端口保持不变；本地原始配置中的对应地址也已同步。
- `${变量名}` 由 Spring 应用进程的环境属性解析。向服务进程提供实际值，或在受控的配置中心填写；不要将填入凭据的文件提交回本目录。
- 敏感变量没有可用的默认凭据。`SPRING_DATASOURCE_USERNAME/PASSWORD`、`SPRING_RABBITMQ_USERNAME/PASSWORD`、`SPRING_ELASTICSEARCH_USERNAME/PASSWORD` 分别对应相应服务账户。
- 对象存储沿用现有拼写 `minio.secreKey`，对应 `MINIO_SECRE_KEY`；视频点播使用 `VOD_SECRET_ID`、`VOD_SECRET_KEY` 等变量；微信和支付使用文件中列出的 `WECHAT_*` 变量。
- 支付配置的 `WECHAT_V3PAY_PRIVATE_KEY_PATH` 应指向本机或容器挂载的私钥文件，不应指向 JAR 内资源。
- 原导出中的 RabbitMQ `cknowledge-mode` 拼写已在模板中修正为 `acknowledge-mode`。原始本地导出未被修改。
- `service-system-dev.yaml` 和对应网关路由仅供未来接入；仓库当前没有系统服务实现。

本地原始配置仍位于 `tingshu-parent/资料/DEFAULT_GROUP/`，已被 Git 忽略。环境变量仅需为当前启动的服务及实际使用的能力配置。

本地文件修改不会自动更新配置中心或正在运行的服务。需同步 Nacos 配置并按需重启服务；微信支付回调还需确保所配置的地址可被微信服务器访问，内网地址不能直接作为公网回调入口。
