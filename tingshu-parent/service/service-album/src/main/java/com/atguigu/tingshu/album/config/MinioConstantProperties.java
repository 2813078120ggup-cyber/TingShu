package com.atguigu.tingshu.album.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix="minio") //读取节点
@Data
public class MinioConstantProperties {

    //@Value("${minio.endpointUrl}")
    private String endpointUrl;

    private String accessKey;
    private String secreKey;
    private String bucketName;
}
