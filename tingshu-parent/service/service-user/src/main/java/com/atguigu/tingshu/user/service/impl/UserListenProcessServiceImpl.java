package com.atguigu.tingshu.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.rabbit.constant.MqConst;
import com.atguigu.tingshu.common.rabbit.service.RabbitService;
import com.atguigu.tingshu.common.util.MongoUtil;
import com.atguigu.tingshu.model.user.UserListenProcess;
import com.atguigu.tingshu.user.service.UserListenProcessService;
import com.atguigu.tingshu.vo.album.TrackStatMqVo;
import com.atguigu.tingshu.vo.user.UserListenProcessVo;
import org.bson.types.ObjectId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class UserListenProcessServiceImpl implements UserListenProcessService {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RabbitService rabbitService;
    
    
    // 获取用户上次播放的进度
    @Override
    public BigDecimal getTrackBreakSecond(Long userId, Long trackId) {
        //	根据用户Id,声音Id获取播放进度对象
        Query query = Query.query(Criteria.where("userId").is(userId).and("trackId").is(trackId));
        // 需求：为每个用户创建表，把这个用户播放进度存在自己表里面
        UserListenProcess userListenProcess = mongoTemplate
                .findOne(query,
                        UserListenProcess.class,
                        MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, userId));
        //	判断
        if (null != userListenProcess) {
            //	获取到播放的跳出时间
            return userListenProcess.getBreakSecond();
        }
        return new BigDecimal("0");
    }
    
    // 更新播放进度
    
    @Override
    public void updateListenProcess(Long userId, UserListenProcessVo userListenProcessVo) {
        log.info("updateListenProcess 入口：userId={}，trackId={}，albumId={}",
                userId, userListenProcessVo.getTrackId(), userListenProcessVo.getAlbumId());
        // 1. 查询当前用户id+声音id是否有播放进度
        Criteria criteria = Criteria.where("userId")
                .is(userId).and("trackId").is(userListenProcessVo.getTrackId());
        Query query = Query.query(criteria);
        UserListenProcess userListenProcess = this.mongoTemplate.findOne(query,
                UserListenProcess.class,
                MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, userId));
        // 2. 判断：如果有播放进度，进行更新
        if (null != userListenProcess) {
            //	设置更新时间
            userListenProcess.setUpdateTime(new Date());
            //	设置跳出时间
            userListenProcess.setBreakSecond(userListenProcessVo.getBreakSecond());
            //	存储数据
            mongoTemplate.save(userListenProcess, MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, userId));
        } else { // 3. 没有播放进度，创建
            //	创建对象
            userListenProcess = new UserListenProcess();
            //	进行属性拷贝
            BeanUtils.copyProperties(userListenProcessVo, userListenProcess);
            //	设置Id
            userListenProcess.setId(ObjectId.get().toString());
            //	设置用户Id
            userListenProcess.setUserId(userId);
            //	设置是否显示
            userListenProcess.setIsShow(1);
            //	创建时间
            userListenProcess.setCreateTime(new Date());
            //	更新时间
            userListenProcess.setUpdateTime(new Date());
            //	保存数据
            mongoTemplate.save(userListenProcess,
                    MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, userId));
        }
        // 4. 更新声音播放量
        // 同一用户对于一个声音，24小时记录一次播放量
        // 专辑的播放量 = 声音播放量的总和   声音：用户
        String key = "user:track:" + userListenProcessVo.getTrackId() + userId;
        // getbit key offset offset=trackId;
        // 获取播放量统计的标识
        Boolean isExist = redisTemplate.opsForValue().getBit(key, userListenProcessVo.getTrackId());
        log.info("播放去重标识：key={}，trackId={}，isExist={}", key, userListenProcessVo.getTrackId(), isExist);
        // !isExist 表示没有标识 -> 第一次播放
        if (!isExist) {
            // 设置播放量统计的标识
            redisTemplate.opsForValue().setBit(key, userListenProcessVo.getTrackId(), true); // true:1  false:0
            // 设置key 的过期时间24小时
            redisTemplate.expire(key, 24 * 60 * 60, TimeUnit.SECONDS);
            
            // 发送消息，更新播放量统计
            TrackStatMqVo trackStatMqVo = new TrackStatMqVo();
            trackStatMqVo.setBusinessNo(UUID.randomUUID().toString().replaceAll("-", ""));
            trackStatMqVo.setAlbumId(userListenProcessVo.getAlbumId());
            trackStatMqVo.setTrackId(userListenProcessVo.getTrackId());
            trackStatMqVo.setStatType(SystemConstant.TRACK_STAT_PLAY);
            trackStatMqVo.setCount(1);
            rabbitService.sendMessage(MqConst.EXCHANGE_TRACK, MqConst.ROUTING_TRACK_STAT_UPDATE, JSON.toJSONString(trackStatMqVo));
            log.info("播放量MQ消息已发送：{}", JSON.toJSONString(trackStatMqVo));
        }
    }
}
