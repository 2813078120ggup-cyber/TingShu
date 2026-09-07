package com.atguigu.tingshu.user.service;

import com.atguigu.tingshu.vo.user.UserListenProcessVo;

import java.math.BigDecimal;

public interface UserListenProcessService {
    
    // 获取用户听声音的上次跳出时间
    BigDecimal getTrackBreakSecond(Long userId, Long trackId);
    
    // 更新播放进度
    void updateListenProcess(Long userId, UserListenProcessVo userListenProcessVo);
}
