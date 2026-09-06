package com.atguigu.tingshu.user.service;

import com.atguigu.tingshu.model.user.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface UserInfoService extends IService<UserInfo> {
    
    // 根据用户id查询用户是否购买过声音列表
    Map<Long, Integer> userIsPaidTrack(Long userId, Long albumId, List<Long> trackIdList);
}
