package com.atguigu.tingshu.order.api;

import com.atguigu.tingshu.common.login.TingShuLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.order.service.OrderInfoService;
import com.atguigu.tingshu.vo.order.OrderInfoVo;
import com.atguigu.tingshu.vo.order.TradeVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "订单管理")
@RestController
@RequestMapping("api/order/orderInfo")
@SuppressWarnings({"all"})
public class OrderInfoApiController {
    
    @Autowired
    private OrderInfoService orderInfoService;
    
    @TingShuLogin
    @Operation(summary = "确认订单")
    @PostMapping("trade")
    public Result<OrderInfoVo> trade(@RequestBody @Validated TradeVo tradeVo) {
        // 获取到用户Id
        Long userId = AuthContextHolder.getUserId();
        // 调用服务层方法
        OrderInfoVo orderInfoVo = orderInfoService.trade(tradeVo, userId);
        // 返回数据
        return Result.ok(orderInfoVo);
    }
    
    
    
}
