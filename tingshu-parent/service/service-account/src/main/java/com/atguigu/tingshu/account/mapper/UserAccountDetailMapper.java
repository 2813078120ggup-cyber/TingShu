package com.atguigu.tingshu.account.mapper;

import com.atguigu.tingshu.model.account.UserAccountDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAccountDetailMapper extends BaseMapper<UserAccountDetail> {
    
    IPage<UserAccountDetail> selectUserRechargePage(Page<UserAccountDetail> pageParam, Long userId);
    
    IPage<UserAccountDetail> selectUserConsumePage(Page<UserAccountDetail> pageParam, Long userId);
}
