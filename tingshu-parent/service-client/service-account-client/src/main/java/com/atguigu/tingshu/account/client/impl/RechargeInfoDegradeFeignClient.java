package com.atguigu.tingshu.account.client.impl;


import com.atguigu.tingshu.account.client.RechargeInfoFeignClient;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.account.RechargeInfo;
import org.springframework.stereotype.Component;

@Component
public class RechargeInfoDegradeFeignClient implements RechargeInfoFeignClient {
    /**
     * @param orderNo
     * @return
     */
    @Override
    public Result<RechargeInfo> getRechargeInfo(String orderNo) {
        return null;
    }
}
