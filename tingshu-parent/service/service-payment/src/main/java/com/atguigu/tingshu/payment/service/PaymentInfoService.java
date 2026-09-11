package com.atguigu.tingshu.payment.service;

import com.atguigu.tingshu.model.payment.PaymentInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wechat.pay.java.service.payments.model.Transaction;

public interface PaymentInfoService extends IService<PaymentInfo> {
    
    // 添加支付记录到payment_info
    PaymentInfo savePaymentInfo(String paymentType, String orderNo);
    
    void updatePaymentStatus(Transaction transaction);
}
