package com.atguigu.tingshu.user.strategy.impl;

import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.model.user.UserVipService;
import com.atguigu.tingshu.model.user.VipServiceConfig;
import com.atguigu.tingshu.user.mapper.UserInfoMapper;
import com.atguigu.tingshu.user.mapper.UserVipServiceMapper;
import com.atguigu.tingshu.user.mapper.VipServiceConfigMapper;
import com.atguigu.tingshu.user.strategy.ItemTypeStrategy;
import com.atguigu.tingshu.vo.user.UserPaidRecordVo;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component("1003")
public class VipItemTypeStrategyImpl implements ItemTypeStrategy {
    @Autowired
    private UserInfoMapper userInfoMapper;
    
    
    @Autowired
    private VipServiceConfigMapper vipServiceConfigMapper;
    
    
    @Autowired
    private UserVipServiceMapper userVipServiceMapper;
    
    /**
     * 处理购买项目类型为：VIP会员
     *
     * @param userPaidRecordVo
     */
    @Override
    public void savePaidRecord(UserPaidRecordVo userPaidRecordVo) {
        log.info("处理购买项目类型为：VIP会员");
        //3.判断购买项目类型-处理VIP会员-允许多次购买
        //3.1 新增VIP购买记录
        UserVipService userVipService = new UserVipService();
        //3.1.1 根据VIP套餐ID查询套餐信息-得到VIP会员服务月数
        Long vipConfigId = userPaidRecordVo.getItemIdList().get(0);
        VipServiceConfig vipServiceConfig = vipServiceConfigMapper.selectById(vipConfigId);
        //3.1.2 获取用户身份，如果是VIP会员，则续费
        UserInfo userInfo = userInfoMapper.selectById(userPaidRecordVo.getUserId());
        //  当前系统时间
        Date currentTime = new Date();
        if (userInfo.getIsVip().intValue() == 1 && userInfo.getVipExpireTime().after(new Date())) {
            //  设置vip 续费的起始时间
            currentTime = userInfo.getVipExpireTime();
        }
        currentTime = new LocalDateTime(currentTime).plusMonths(vipServiceConfig.getServiceMonth()).toDate();
        //  设置过期时间
        userVipService.setExpireTime(currentTime);
        userVipService.setStartTime(new Date());
        //3.1.4 构建VIP购买记录对象保存
        userVipService.setUserId(userPaidRecordVo.getUserId());
        userVipService.setOrderNo(userPaidRecordVo.getOrderNo());
        userVipServiceMapper.insert(userVipService);
        
        //3.2 更新用户表中VIP状态及会员过期时间
        userInfo.setIsVip(1);
        userInfo.setVipExpireTime(userVipService.getExpireTime());
        userInfoMapper.updateById(userInfo);
    }
}