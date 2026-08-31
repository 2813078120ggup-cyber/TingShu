package com.atguigu.tingshu.system.client.impl;


import com.atguigu.tingshu.system.client.SysLoginLogFeignClient;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.system.SysLoginLog;
import org.springframework.stereotype.Component;

@Component
public class SysLoginLogDegradeFeignClient implements SysLoginLogFeignClient {


    @Override
    public Result recordLoginLog(SysLoginLog sysLoginLog) {
        return null;
    }
}
