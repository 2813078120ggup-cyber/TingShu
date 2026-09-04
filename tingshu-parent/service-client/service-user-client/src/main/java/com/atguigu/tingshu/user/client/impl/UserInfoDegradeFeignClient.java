package com.atguigu.tingshu.user.client.impl;


import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.user.client.UserInfoFeignClient;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import org.springframework.stereotype.Component;

@Component
public class UserInfoDegradeFeignClient implements UserInfoFeignClient {
    
    @Override
    public Result<UserInfoVo> getUserInfoVo(Long userId) {
        return Result.fail();
    }
}
