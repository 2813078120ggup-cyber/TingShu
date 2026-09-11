package com.atguigu.tingshu.payment.service;

import com.wechat.pay.java.service.payments.model.Transaction;

import java.util.Map;

public interface WxPayService {
    
    // 微信支付
    Map createJsapi(String paymentType, String orderNo, Long userId);
    
    Transaction queryPayStatus(String orderNo);
}
