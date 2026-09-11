package com.atguigu.tingshu.payment.service;

import com.wechat.pay.java.service.payments.model.Transaction;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface WxPayService {
    
    // 微信支付
    Map createJsapi(String paymentType, String orderNo, Long userId);
    
    Transaction queryPayStatus(String orderNo);
    
    void wxnotify(HttpServletRequest request);
    
    Map<String, Object> createNative(String paymentType, String orderNo, Long userId);
}
