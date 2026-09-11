package com.atguigu.tingshu.account.service;

import com.atguigu.tingshu.model.account.RechargeInfo;
import com.atguigu.tingshu.vo.account.RechargeInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface RechargeInfoService extends IService<RechargeInfo> {
    
    // 充值
    String submitRecharge(RechargeInfoVo rechargeInfoVo, Long userId);
    
    // 根据订单号获取充值信息
    RechargeInfo getRechargeInfoByOrderNo(String orderNo);
    
    // 根据订单号获取充值信息
    void rechargePaySuccess(String orderNo);
}
