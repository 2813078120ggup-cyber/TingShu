package com.atguigu.tingshu.live.message;

import com.alibaba.fastjson.JSON;
import com.atguigu.tingshu.live.util.WebSocketLocalContainerUtil;
import com.atguigu.tingshu.model.live.SocketMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * redis广播消息处理类
 */
@Component
public class RedisMessageReceive {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 接收redis广播消息的方法
     */
    public void receiveMessage(String message) {
        System.out.println("----------收到消息了message：" + message);
        Object msg = redisTemplate.getValueSerializer().deserialize(message.getBytes());
        System.out.println("----------收到消息了message1：" + msg);
        if (!StringUtils.isEmpty(message)) {
            // 从客户端传过来的数据是json数据，所以这里使用JSON进行转换为SocketMsg对象，
            SocketMsg socketMsg = JSON.parseObject(msg.toString(), SocketMsg.class);
            // 忽略心跳消息
            if (!SocketMsg.MsgTypeEnum.HEART_BEAT.getCode().equals(socketMsg.getMsgType())) {
                WebSocketLocalContainerUtil.sendMsg(socketMsg);
            }
        }
    }
}