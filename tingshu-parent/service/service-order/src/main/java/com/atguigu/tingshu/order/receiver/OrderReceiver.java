package com.atguigu.tingshu.order.receiver;


import com.atguigu.tingshu.common.rabbit.constant.MqConst;
import com.atguigu.tingshu.order.service.OrderInfoService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class OrderReceiver {
    
    @Autowired
    private OrderInfoService orderInfoService;
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = MqConst.EXCHANGE_ORDER, durable = "true"),
            value = @Queue(value = MqConst.QUEUE_ORDER_PAY_SUCCESS, durable = "true"),
            key = {MqConst.ROUTING_ORDER_PAY_SUCCESS}
    ))
    public void orderPaySuccess(String orderNo, Message message, Channel channel) {
        //业务处理
        if (StringUtils.hasText(orderNo)) {
            orderInfoService.orderPaySuccess(orderNo);
        }
        
        //手动应答
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}