package com.atguigu.tingshu.album.receiver;

import com.alibaba.fastjson.JSON;
import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.common.rabbit.constant.MqConst;
import com.atguigu.tingshu.vo.album.TrackStatMqVo;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class TrackReceiver {
    @Autowired
    private TrackInfoService trackInfoService;
    
    @Autowired
    private RedisTemplate redisTemplate;
    
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = MqConst.EXCHANGE_TRACK, durable = "true"),
            value = @Queue(value = MqConst.QUEUE_TRACK_STAT_UPDATE, durable = "true"),
            key = {MqConst.ROUTING_TRACK_STAT_UPDATE}
    ))
    public void updateStat(String content, Message message, Channel channel) {
        
        if (StringUtils.hasText(content)) {
            TrackStatMqVo trackStatMqVo = JSON.parseObject(content, TrackStatMqVo.class);
            log.info("更新声音统计：" + JSON.toJSONString(trackStatMqVo));
            //业务处理
            if (null != trackStatMqVo) {
                log.info("更新声音统计：" + JSON.toJSONString(trackStatMqVo));
                //业务去重
                String key = trackStatMqVo.getBusinessNo();
                boolean isExist = redisTemplate.opsForValue().setIfAbsent(key, 1, 1, TimeUnit.HOURS);
                if (isExist) {
                    try {
                        trackInfoService.updateStat(trackStatMqVo.getAlbumId(), trackStatMqVo.getTrackId(), trackStatMqVo.getStatType(), trackStatMqVo.getCount());
                    } catch (Exception e) {
                        redisTemplate.delete(key);
                        e.printStackTrace();
                    }
                }
            }
        }
        
        //手动应答
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
    
}