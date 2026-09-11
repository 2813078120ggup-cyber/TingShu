package com.atguigu.tingshu.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.order.OrderInfo;
import com.atguigu.tingshu.model.payment.PaymentInfo;
import com.atguigu.tingshu.order.client.OrderInfoFeignClient;
import com.atguigu.tingshu.payment.config.WxPayV3Config;
import com.atguigu.tingshu.payment.service.PaymentInfoService;
import com.atguigu.tingshu.payment.service.WxPayService;
import com.atguigu.tingshu.user.client.UserInfoFeignClient;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.*;
import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WxPayServiceImpl implements WxPayService {
    
    @Autowired
    private PaymentInfoService paymentInfoService;
    
    // 发起微信支付请求
    @Autowired
    private RSAAutoCertificateConfig rsaAutoCertificateConfig;
    @Autowired
    private OrderInfoFeignClient orderInfoFeignClient;
    @Autowired
    private WxPayV3Config wxPayV3Config;
    @Autowired
    private UserInfoFeignClient userInfoFeignClient;
    
    
    //	支付文档入口：https://developers.weixin.qq.com/miniprogram/dev/api/payment/wx.requestPayment.html
//	对接方式：https://github.com/wechatpay-apiv3/wechatpay-java
    @Override
    public Map createJsapi(String paymentType, String orderNo, Long userId) {
        try {
            // 1. 根据paymentType判断是订单不是充值
            //	充值业务不需要判断当前订单是否取消
            if (!paymentType.equals(SystemConstant.PAYMENT_TYPE_RECHARGE)) {
                //	查询当前订单状态，如果订单状态已经取消，则不能生成二维码！
                // 远程调用：根据订单编号查询订单对象
                Result<OrderInfo> orderInfoResult =
                        this.orderInfoFeignClient.getOrderInfo(orderNo);
                
                Assert.notNull(orderInfoResult, "查询订单返回结果不能为空");
                
                OrderInfo orderInfo = orderInfoResult.getData();
                
                Assert.notNull(orderInfo, "订单对象不能为空");
                // 判断订单状态，如果订单为取消状态，则不需要支付
                if (SystemConstant.ORDER_STATUS_CANCEL.equals(orderInfo.getOrderStatus())) {
                    return null;
                }
            }
            
            
            // 2. 添加支付记录到payment_info (当前状态：未支付
            PaymentInfo paymentInfo = paymentInfoService.savePaymentInfo(paymentType, orderNo);
            // 3. 发起支付请求
            JsapiServiceExtension service = new JsapiServiceExtension.Builder().config(rsaAutoCertificateConfig).build();
            // 创建request对象，传入参数
            PrepayRequest request = new PrepayRequest();
            // 注意在这个包下 com.wechat.pay.java.service.payments.jsapi.model.Amount
            Amount amount = new Amount();
            amount.setTotal(1);
            request.setAmount(amount);
            request.setAppid(wxPayV3Config.getAppid());
            request.setMchid(wxPayV3Config.getMerchantId());
            // 微信显示信息
            request.setDescription(paymentInfo.getContent());
            // 支付回调地址
            request.setNotifyUrl(wxPayV3Config.getNotifyUrl());
            // 订单编号
            request.setOutTradeNo(paymentInfo.getOrderNo());
            // 远程调用：获取用户信息
            Result<UserInfoVo> userInfoVoResult = userInfoFeignClient.getUserInfoVo(paymentInfo.getUserId());
            Assert.notNull(userInfoVoResult, "返回用户结果集对象不能为空");
            UserInfoVo userInfoVo = userInfoVoResult.getData();
            Assert.notNull(userInfoVo, "用户对象不能为空");
            String openid = userInfoVo.getWxOpenId();
            Payer payer = new Payer();
            payer.setOpenid(openid);
            request.setPayer(payer);
            
            // 调用下单方法，得到应答
            // response包含了调起支付所需的所有参数，可直接用于前端调起支付
            // 调用方法，传入request对象，发起支付请求，得到response对象
            PrepayWithRequestPaymentResponse response = service.prepayWithRequestPayment(request);
            log.info("微信支付下单返回参数：{}", JSON.toJSONString(response));
            Map<String, Object> result = new HashMap();
            result.put("timeStamp", response.getTimeStamp());
            result.put("nonceStr", response.getNonceStr());
            result.put("package", response.getPackageVal());
            result.put("signType", response.getSignType());
            result.put("paySign", response.getPaySign());
            //	返回数据
            return result;
        } catch (ServiceException e) {
            e.printStackTrace();
            throw new GuiguException(201, e.getErrorMessage());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new GuiguException(201, "订单号不存在");
        } catch (Exception e) {
            e.printStackTrace();
            throw new GuiguException(201, "微信下单异常");
        }
    }
    
    @Override
    public Transaction queryPayStatus(String orderNo) {
        try {
            //	构建service
            JsapiServiceExtension service = new JsapiServiceExtension.Builder().config(rsaAutoCertificateConfig).build();
            QueryOrderByOutTradeNoRequest queryRequest = new QueryOrderByOutTradeNoRequest();
            queryRequest.setMchid(wxPayV3Config.getMerchantId());
            queryRequest.setOutTradeNo(orderNo);
            
            Transaction result = service.queryOrderByOutTradeNo(queryRequest);
            log.info("Transaction:\t" + JSON.toJSONString(result));
            return result;
        } catch (ServiceException e) {
            // API返回失败, 例如ORDER_NOT_EXISTS
            System.out.printf("code=[%s], message=[%s]\n", e.getErrorCode(), e.getErrorMessage());
            System.out.printf("reponse body=[%s]\n", e.getResponseBody());
            e.printStackTrace();
        }
        return null;
    }
}
