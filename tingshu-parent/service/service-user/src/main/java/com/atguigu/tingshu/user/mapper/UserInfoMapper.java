package com.atguigu.tingshu.user.mapper;

import com.atguigu.tingshu.model.user.UserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
    
    /**
     * 更新用户是否为VIP
     */
    @Update("update user_info " +
            "set is_vip = 0 " +
            "where is_vip = 1 " +
            "and vip_expire_time < now() " +
            "and is_deleted = 0")
    void updateIsVip();
    
}
