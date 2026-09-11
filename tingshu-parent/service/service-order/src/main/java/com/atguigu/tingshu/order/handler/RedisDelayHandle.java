package com.atguigu.tingshu.order.handler;

import com.atguigu.tingshu.common.rabbit.constant.MqConst;
import com.atguigu.tingshu.order.service.OrderInfoService;
import jakarta.annotation.PostConstruct;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RedisDelayHandle {
    
    @Autowired
    private RedissonClient redissonClient;
    
    @Autowired
    private OrderInfoService orderInfoService;
    
    @PostConstruct
    public void listener() {
        //创建线程
        new Thread(() -> {
            //一直监听
            while (true) {
                
                try {
                    // 从 Redis 中获取一个 RBlockingQueue
                    RBlockingQueue<String> blockingQueue =
                            redissonClient.getBlockingQueue(MqConst.EXCHANGE_CANCEL_ORDER);
                    // 延迟队列中获取消息
                    String id = blockingQueue.take();
                    // 如果可以得到，根据消息取消订单
                    if (StringUtils.hasText(id)) {
                        // 调用方法，取消订单
                        orderInfoService.orderCancel(Long.parseLong(id));
                    }
                    
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}