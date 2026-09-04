package com.atguigu.tingshu.search.receiver;

import com.atguigu.tingshu.common.rabbit.constant.MqConst;
import com.atguigu.tingshu.search.service.SearchService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @className: SearchReceiver
 * @author: gc
 * @date: 2026/9/4 21:09
 * @version: 1.0
 * @description:
 */
@Component
public class SearchReceiver {
    @Autowired
    private SearchService searchService;
    
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = MqConst.EXCHANGE_ALBUM, durable = "true"),
            value = @Queue(value = MqConst.QUEUE_ALBUM_UPPER, durable = "true"),
            key = {MqConst.ROUTING_ALBUM_UPPER}
    ))
    public void upperAlbum(Long albumId, Message message, Channel channel) {
        if (albumId != null) {
            searchService.upperAlbum(albumId);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
    
    
    /**
     * 专辑下架
     *
     * @param albumId
     * @param message
     * @param channel
     */
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = MqConst.EXCHANGE_ALBUM, durable = "true"),
            value = @Queue(value = MqConst.QUEUE_ALBUM_LOWER, durable = "true"),
            key = {MqConst.ROUTING_ALBUM_LOWER}
    ))
    public void lowerGoods(Long albumId, Message message, Channel channel) {
        //业务处理
        if (null != albumId) {
            searchService.lowerAlbum(albumId);
        }
        
        //手动应答
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}