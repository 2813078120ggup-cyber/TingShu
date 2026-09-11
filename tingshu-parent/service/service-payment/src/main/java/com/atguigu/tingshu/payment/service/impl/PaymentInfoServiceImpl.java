package com.atguigu.tingshu.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.tingshu.account.client.RechargeInfoFeignClient;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.rabbit.constant.MqConst;
import com.atguigu.tingshu.common.rabbit.service.RabbitService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.account.RechargeInfo;
import com.atguigu.tingshu.model.order.OrderInfo;
import com.atguigu.tingshu.model.payment.PaymentInfo;
import com.atguigu.tingshu.order.client.OrderInfoFeignClient;
import com.atguigu.tingshu.payment.mapper.PaymentInfoMapper;
import com.atguigu.tingshu.payment.service.PaymentInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wechat.pay.java.service.payments.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;

@Service
@SuppressWarnings({"all"})
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {
    
    @Autowired
    private OrderInfoFeignClient orderInfoFeignClient;
    @Autowired
    private RechargeInfoFeignClient rechargeInfoFeignClient;
    
    // 保存支付记录
    
    /**
     * @param paymentType
     * @param orderNo
     * @return
     */
    @Override
    public PaymentInfo savePaymentInfo(String paymentType, String orderNo) {
        // 1. 根据订单编号，查询支付记录表
        PaymentInfo paymentInfo = this.getOne(new LambdaQueryWrapper<PaymentInfo>().eq(PaymentInfo::getOrderNo, orderNo));
        if (null == paymentInfo) {
            //  创建对象
            paymentInfo = new PaymentInfo();
            //  支付信息
            // 2. 判断：如果没有查到数据，第一次添加，如果查到数据直接返回
            // 1301-订单
            if (paymentType.equals(SystemConstant.PAYMENT_TYPE_ORDER)) {
                //  远程调用获取到订单对象
                Result<OrderInfo> orderInfoResult = orderInfoFeignClient.getOrderInfo(orderNo);
                Assert.notNull(orderInfoResult, "返回订单对象不能为空");
                OrderInfo orderInfo = orderInfoResult.getData();
                Assert.notNull(orderInfo, "返回订单对象不能为空");
                paymentInfo.setUserId(orderInfo.getUserId());
                paymentInfo.setContent(orderInfo.getOrderTitle());
                paymentInfo.setAmount(orderInfo.getOrderAmount());
            } else {
                // 1302-充值
                //  充值信息
                // 远程调用：充值
                Result<RechargeInfo> rechargeInfoResult = rechargeInfoFeignClient.getRechargeInfo(orderNo);
                Assert.notNull(rechargeInfoResult, "返回充值对象不能不为空");
                RechargeInfo rechargeInfo = rechargeInfoResult.getData();
                Assert.notNull(rechargeInfo, "返回充值对象不能不为空");
                paymentInfo.setUserId(rechargeInfo.getUserId());
                paymentInfo.setContent("充值");
                paymentInfo.setAmount(rechargeInfo.getRechargeAmount());
            }
            paymentInfo.setPaymentType(paymentType);
            paymentInfo.setOrderNo(orderNo);
            paymentInfo.setPaymentStatus(SystemConstant.PAYMENT_STATUS_UNPAID);
            this.save(paymentInfo);
        }
        return paymentInfo;
    }
    
    @Autowired
    private RabbitService rabbitService;
    
    @Override
    public void updatePaymentStatus(Transaction transaction) {
        PaymentInfo paymentInfo = this.getOne(new LambdaQueryWrapper<PaymentInfo>().eq(PaymentInfo::getOrderNo, transaction.getOutTradeNo()));
        if (paymentInfo.getPaymentStatus() == SystemConstant.PAYMENT_STATUS_PAID) {
            return;
        }
        
        //更新支付信息
        paymentInfo.setPaymentStatus(SystemConstant.PAYMENT_STATUS_PAID);
        paymentInfo.setOrderNo(transaction.getOutTradeNo());
        paymentInfo.setOutTradeNo(transaction.getTransactionId());
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(JSON.toJSONString(transaction));
        this.updateById(paymentInfo);
        // 表示交易成功！
        
        // 后续更新订单状态！ 使用消息队列！
        String routing = paymentInfo.getPaymentType().equals(SystemConstant.PAYMENT_TYPE_ORDER) ? MqConst.ROUTING_ORDER_PAY_SUCCESS : MqConst.ROUTING_RECHARGE_PAY_SUCCESS;
        rabbitService.sendMessage(MqConst.EXCHANGE_ORDER, routing, paymentInfo.getOrderNo());
    }
}
