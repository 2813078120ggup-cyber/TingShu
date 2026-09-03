package com.atguigu.tingshu.account.receiver;

import com.atguigu.tingshu.account.service.UserAccountService;
import com.atguigu.tingshu.common.rabbit.constant.MqConst;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @className: AccountReceiver
 * @author: gc
 * @date: 2026/9/3 17:10
 * @version: 1.0
 * @description:
 */
@Slf4j
@Component
public class AccountReceiver {
    
    @Autowired
    private UserAccountService userAccountService;
    
    /**
     * 注册成功初始化用户账户信息
     *
     * @param userId
     * @param message
     * @param channel
     */
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = MqConst.EXCHANGE_USER, durable = "true"),
            value = @Queue(value = MqConst.QUEUE_USER_REGISTER, durable = "true"),
            key = {MqConst.ROUTING_USER_REGISTER}
    ))
    public void addUserAccount(Long userId, Message message, Channel channel) {
        //业务处理
        if (null != userId) {
            log.info("注册成功初始化用户账户信息：{}", userId);
            //注册成功初始化用户账户信息
            userAccountService.addUserAccount(userId);
        }
        //手动应答
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}