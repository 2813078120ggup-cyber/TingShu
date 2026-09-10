package com.atguigu.tingshu.user.client.impl;


import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.user.VipServiceConfig;
import com.atguigu.tingshu.user.client.VipServiceConfigFeignClient;
import org.springframework.stereotype.Component;

@Component
public class VipServiceConfigDegradeFeignClient implements VipServiceConfigFeignClient {
    
    @Override
    public Result<Boolean> isPaidAlbum(Long albumId) {
        return null;
    }
    
    @Override
    public Result<VipServiceConfig> getVipServiceConfig(Long id) {
        return null;
    }
}
