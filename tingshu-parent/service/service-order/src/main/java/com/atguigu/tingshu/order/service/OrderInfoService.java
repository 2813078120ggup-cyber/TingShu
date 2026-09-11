package com.atguigu.tingshu.order.service;

import com.atguigu.tingshu.model.order.OrderInfo;
import com.atguigu.tingshu.vo.order.OrderInfoVo;
import com.atguigu.tingshu.vo.order.TradeVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OrderInfoService extends IService<OrderInfo> {
    
    // 确认订单
    OrderInfoVo trade(TradeVo tradeVo, Long userId);
    
    // 提交订单
    String submitOrder(OrderInfoVo orderInfoVo, Long userId);
    
    // 取消订单
    void orderCancel(long l);
    
    // 查看我的订单
    IPage<OrderInfo> findUserPage(Page<OrderInfo> pageParam, Long userId);
    
    // 根据订单号获取订单信息
    OrderInfo getOrderInfoByOrderNo(String orderNo);
    
    void orderPaySuccess(String orderNo);
}
