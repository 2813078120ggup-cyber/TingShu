package com.atguigu.tingshu.user.strategy.impl;


import com.atguigu.tingshu.model.user.UserPaidAlbum;
import com.atguigu.tingshu.user.mapper.UserPaidAlbumMapper;
import com.atguigu.tingshu.user.strategy.ItemTypeStrategy;
import com.atguigu.tingshu.vo.user.UserPaidRecordVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("1001")
public class AlbumItemTypeStrategyImpl implements ItemTypeStrategy {
    //将专辑类型购买策略实现类beanId跟前端提交购买项目类型一致
    @Autowired
    private UserPaidAlbumMapper userPaidAlbumMapper;
    
    /**
     * 处理购买项目类型为：专辑
     *
     * @param userPaidRecordVo
     */
    @Override
    public void savePaidRecord(UserPaidRecordVo userPaidRecordVo) {
        log.info("处理购买项目类型为：专辑");
        //1.1 根据订单编号查询专辑购买记录
        LambdaQueryWrapper<UserPaidAlbum> userPaidAlbumLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userPaidAlbumLambdaQueryWrapper.eq(UserPaidAlbum::getOrderNo, userPaidRecordVo.getOrderNo());
        Long count = userPaidAlbumMapper.selectCount(userPaidAlbumLambdaQueryWrapper);
        if (count > 0) {
            return;
        }
        //1.2 查询到专辑购买记录为空则新增购买记录
        UserPaidAlbum userPaidAlbum = new UserPaidAlbum();
        userPaidAlbum.setOrderNo(userPaidRecordVo.getOrderNo());
        userPaidAlbum.setUserId(userPaidRecordVo.getUserId());
        userPaidAlbum.setAlbumId(userPaidRecordVo.getItemIdList().get(0));
        userPaidAlbumMapper.insert(userPaidAlbum);
    }
}