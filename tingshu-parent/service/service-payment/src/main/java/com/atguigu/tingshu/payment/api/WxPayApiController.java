package com.atguigu.tingshu.payment.api;

import com.alibaba.fastjson.JSON;
import com.atguigu.tingshu.common.login.TingShuLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.payment.service.PaymentInfoService;
import com.atguigu.tingshu.payment.service.WxPayService;
import com.wechat.pay.java.service.payments.model.Transaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "微信支付接口")
@RestController
@RequestMapping("api/payment/wxPay")
@Slf4j
public class WxPayApiController {
    
    @Autowired
    private WxPayService wxPayService;
    // Request URL: http://localhost/api/payment/wxPay/createJsapi/1301/1f33ddeac9d34c349e09335b6752fb86
    //Request Method: POST
    
    /**
     * 微信支付
     *
     * @param paymentType 支付类型：1301-订单 1302-充值
     * @param orderNo     订单号
     * @return
     */
    @TingShuLogin
    @Operation(summary = "微信下单")
    @Parameters({
            @Parameter(name = "paymentType", description = "支付类型：1301-订单 1302-充值", in = ParameterIn.PATH, required = true),
            @Parameter(name = "orderNo", description = "订单号", required = true, in = ParameterIn.PATH),
    })
    @PostMapping("/createJsapi/{paymentType}/{orderNo}")
    public Result createJsapi(@PathVariable String paymentType, @PathVariable String orderNo) {
        //  调用微信支付方法
        Map map = wxPayService.createJsapi(paymentType, orderNo, AuthContextHolder.getUserId());
        return Result.ok(map);
    }
    
    @Autowired
    private PaymentInfoService paymentInfoService;
    /**
     * 查询支付状态 https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_5_2.shtml
     *
     * @param orderNo
     * @return
     */
    @Operation(summary = "支付状态查询")
    @GetMapping("/queryPayStatus/{orderNo}")
    public Result queryPayStatus(@PathVariable String orderNo) {
        try {
            // 调用查询接口 com.wechat.pay.java.service.payments.model.Transaction;
            Transaction transaction = wxPayService.queryPayStatus(orderNo);
            System.out.println("queryPayStatus: " + JSON.toJSONString(transaction));
            if (null != transaction && transaction.getTradeState() == Transaction.TradeStateEnum.SUCCESS) {
                //更改订单状态
                paymentInfoService.updatePaymentStatus(transaction);
                return Result.ok(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.ok(false);
    }
}