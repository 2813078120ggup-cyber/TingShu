package com.atguigu.tingshu.common.rabbit.service;


import com.alibaba.fastjson.JSON;
import com.atguigu.tingshu.common.rabbit.constant.MqConst;
import com.atguigu.tingshu.common.rabbit.entity.GuiguCorrelationData;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RabbitService {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private RedisTemplate redisTemplate;
    
    @Autowired
    private RedissonClient redissonClient;
    
    
    /**
     * 发送消息
     *
     * @param exchange   交换机
     * @param routingKey 路由键
     * @param message    消息
     */
    public boolean sendMessage(String exchange, String routingKey, Object message) {
        //1.创建自定义相关消息对象-包含业务数据本身，交换器名称，路由键，队列类型，延迟时间,重试次数
        GuiguCorrelationData correlationData = new GuiguCorrelationData();
        String uuid = "mq:" + UUID.randomUUID().toString().replaceAll("-", "");
        correlationData.setId(uuid);
        correlationData.setMessage(message);
        correlationData.setExchange(exchange);
        correlationData.setRoutingKey(routingKey);
        //2.将相关消息封装到发送消息方法中
        
        rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
        
        //3.将相关消息存入Redis  Key：UUID  相关消息对象  10 分钟
        redisTemplate.opsForValue().set(uuid, JSON.toJSONString(correlationData), 10, TimeUnit.MINUTES);
        return true;
    }
    
    //发送消息到延迟队列
    public void sendDealyMessage(Long orderId) {
        //1 创建普通队列
        RBlockingQueue<Object> blockingQueue =
                redissonClient.getBlockingQueue(MqConst.EXCHANGE_CANCEL_ORDER);
        
        //2 把普通队列变成延迟队列
        RDelayedQueue<Object> delayedQueue = redissonClient.getDelayedQueue(blockingQueue);
        
        //3 向延迟队列发送消息，设置延迟时间
        //delayedQueue.offer(id.toString(),30,TimeUnit.MINUTES);
        // 测试 设置10s 实际改为30min
        delayedQueue.offer(orderId.toString(), 30, TimeUnit.MINUTES);
        
    }
}