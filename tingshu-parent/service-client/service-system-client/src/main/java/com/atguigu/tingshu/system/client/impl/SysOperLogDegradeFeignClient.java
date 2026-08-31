package com.atguigu.tingshu.system.client.impl;


import com.atguigu.tingshu.system.client.SysOperLogFeignClient;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.system.SysOperLog;
import org.springframework.stereotype.Component;

@Component
public class SysOperLogDegradeFeignClient implements SysOperLogFeignClient {


    @Override
    public Result saveSysLog(SysOperLog sysOperLog) {
        return null;
    }
}
