package com.atguigu.tingshu.user.service;

import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.vo.user.UserPaidRecordVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface UserInfoService extends IService<UserInfo> {
    
    // 根据用户id查询用户是否购买过声音列表
    Map<Long, Integer> userIsPaidTrack(Long userId, Long albumId, List<Long> trackIdList);
    
    // 判断用户是否购买过专辑
    Boolean isPaidAlbum(Long userId, Long albumId);
    
    // 根据专辑id+用户id获取购买的声音id列表
    List<Long> findUserPaidTrackList(Long userId, Long albumId);
    
    // 添加购买记录
    void savePaidRecord(UserPaidRecordVo userPaidRecordVo);
}
