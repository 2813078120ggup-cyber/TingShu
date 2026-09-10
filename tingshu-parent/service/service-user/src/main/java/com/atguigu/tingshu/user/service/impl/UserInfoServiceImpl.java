package com.atguigu.tingshu.user.service.impl;

import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.model.user.UserPaidAlbum;
import com.atguigu.tingshu.model.user.UserPaidTrack;
import com.atguigu.tingshu.user.mapper.UserInfoMapper;
import com.atguigu.tingshu.user.mapper.UserPaidAlbumMapper;
import com.atguigu.tingshu.user.mapper.UserPaidTrackMapper;
import com.atguigu.tingshu.user.service.UserInfoService;
import com.atguigu.tingshu.user.service.UserPaidTrackService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
    
    @Autowired
    private UserInfoMapper userInfoMapper;
    
    @Autowired
    private UserPaidAlbumMapper userPaidAlbumMapper;
    
    @Autowired
    private UserPaidTrackService userPaidTrackService;
    
    @Autowired
    private UserPaidTrackMapper userPaidTrackMapper;
    
    // 根据用户id查询用户是否购买过声音列表
    /*    传入：
    userId + albumId + trackIdList
            ↓
    先查是否购买整张专辑
            ↓
          买过？
         /     \
       是       否
       ↓         ↓
    所有声音=1  查询单集购买记录
       ↓         ↓
    返回Map    获取已购买trackId
                  ↓
              遍历所有trackId
              /           \
           买过           没买
            ↓              ↓
            1              0
              \           /
                 返回Map*/
    @Override
    public Map<Long, Integer> userIsPaidTrack(Long userId, Long albumId, List<Long> trackIdList) {
        //	1. 根据用户id和专辑id查询用户是否购买专辑 user_paid_album
        LambdaQueryWrapper<UserPaidAlbum> userPaidAlbumLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userPaidAlbumLambdaQueryWrapper.eq(UserPaidAlbum::getUserId, userId).eq(UserPaidAlbum::getAlbumId, albumId);
        UserPaidAlbum userPaidAlbum = userPaidAlbumMapper.selectOne(userPaidAlbumLambdaQueryWrapper);
        //	如果用户购买过专辑，则专辑里面所有声音都被购买 map的key为声音Id，value为1
        if (null != userPaidAlbum) {
            //	创建一个map 集合
            HashMap<Long, Integer> map = new HashMap<>();
            //	如果查询到对应的专辑购买记录，则默认将声音Id 赋值为 1
            trackIdList.forEach(trackId -> {
                map.put(trackId, 1);
            });
            return map;
        } else {
            // 3. 如果用户没有买过专辑，根据用户id+声音id查询用户购买了哪些声音 user_paid_track
            LambdaQueryWrapper<UserPaidTrack> userPaidTrackLambdaQueryWrapper = new LambdaQueryWrapper<>();
            // in UserPaidTrack:用户购买的声音id集合
            userPaidTrackLambdaQueryWrapper.eq(UserPaidTrack::getUserId, userId).in(UserPaidTrack::getTrackId, trackIdList);
            // UserPaidTrack集合
            List<UserPaidTrack> userPaidTrackList = userPaidTrackService.list(userPaidTrackLambdaQueryWrapper);
            // 获取到用户购买声音Id 集合
            List<Long> userPaidTrackIdList = userPaidTrackList.stream()
                    .map(UserPaidTrack::getTrackId).collect(Collectors.toList());
            // 创建一个map 集合
            HashMap<Long, Integer> map = new HashMap<>();
            trackIdList.forEach(trackId -> {
                if (userPaidTrackIdList.contains(trackId)) {
                    //	用户已购买声音
                    map.put(trackId, 1);
                } else {
                    map.put(trackId, 0);
                }
            });
            return map;
        }
    }
    
    
    @Override
    public Boolean isPaidAlbum(Long userId, Long albumId) {
        // 根据用户Id 与专辑Id 查询是否有记录
        LambdaQueryWrapper<UserPaidAlbum> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserPaidAlbum::getUserId, userId).eq(UserPaidAlbum::getAlbumId, albumId);
        Long count = userPaidAlbumMapper.selectCount(lambdaQueryWrapper);
        return count > 0;
    }
    
    // 根据专辑id+用户id获取购买的声音id列表
    
    @Override
    public List<Long> findUserPaidTrackList(Long userId, Long albumId) {
        // 根据用户Id 与 专辑Id 获取到已购买的声音集合
        LambdaQueryWrapper<UserPaidTrack> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserPaidTrack::getUserId, userId)
                .eq(UserPaidTrack::getAlbumId, albumId);
        List<UserPaidTrack> userPaidTrackList = userPaidTrackMapper.selectList(lambdaQueryWrapper);
        // 获取到已购买的声音集合Id 列表
        List<Long> trackIdList = userPaidTrackList.stream().map(UserPaidTrack::getTrackId).collect(Collectors.toList());
        // 返回集合数据
        return trackIdList;
    }
}
