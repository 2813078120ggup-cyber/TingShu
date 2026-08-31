package com.atguigu.tingshu.system.client;

import com.atguigu.tingshu.system.client.impl.SysLoginLogDegradeFeignClient;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.system.SysLoginLog;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * <p>
 * 产品列表API接口
 * </p>
 *
 * 
 */
@FeignClient(value = "service-system", fallback = SysLoginLogDegradeFeignClient.class)
public interface SysLoginLogFeignClient {

    /**
     * 记录登录日志
     * @param sysLoginLog
     * @return
     */
    @PostMapping("/admin/system/sysLoginLog/recordLoginLog")
    Result recordLoginLog(@RequestBody SysLoginLog sysLoginLog);
}