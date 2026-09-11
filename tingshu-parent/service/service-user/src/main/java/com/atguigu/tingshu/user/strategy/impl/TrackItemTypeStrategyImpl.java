package com.atguigu.tingshu.user.strategy.impl;


import com.atguigu.tingshu.album.client.TrackInfoFeignClient;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.model.user.UserPaidTrack;
import com.atguigu.tingshu.user.mapper.UserPaidTrackMapper;
import com.atguigu.tingshu.user.strategy.ItemTypeStrategy;
import com.atguigu.tingshu.vo.user.UserPaidRecordVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("1002")
public class TrackItemTypeStrategyImpl implements ItemTypeStrategy {
    @Autowired
    private UserPaidTrackMapper userPaidTrackMapper;
    
    @Autowired
    private TrackInfoFeignClient trackInfoFeignClient;
    
    
    /**
     * 处理购买项目类型为：声音
     *
     * @param userPaidRecordVo
     */
    @Override
    public void savePaidRecord(UserPaidRecordVo userPaidRecordVo) {
        log.info("处理购买项目类型为：声音");
        //2.判断购买项目类型-处理声音
        //2.1 根据订单编号查询声音购买记录
        LambdaQueryWrapper<UserPaidTrack> userPaidTrackLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userPaidTrackLambdaQueryWrapper.eq(UserPaidTrack::getOrderNo, userPaidRecordVo.getOrderNo());
        Long count = userPaidTrackMapper.selectCount(userPaidTrackLambdaQueryWrapper);
        if (count > 0) {
            return;
        }
        //2.2 查询到声音购买记录为空则新增购买记录（循环批量新增）
        //2.2.1 远程调用专辑服务-根据声音ID查询声音对象-获取声音所属专辑ID
        TrackInfo trackInfo = trackInfoFeignClient.getTrackInfo(userPaidRecordVo.getItemIdList().get(0)).getData();
        Long albumId = trackInfo.getAlbumId();
        //2.2.2 遍历购买项目ID集合批量新增声音购买记录
        userPaidRecordVo.getItemIdList().forEach(trackId -> {
            UserPaidTrack userPaidTrack = new UserPaidTrack();
            userPaidTrack.setOrderNo(userPaidRecordVo.getOrderNo());
            userPaidTrack.setUserId(userPaidRecordVo.getUserId());
            userPaidTrack.setAlbumId(albumId);
            userPaidTrack.setTrackId(trackId);
            userPaidTrackMapper.insert(userPaidTrack);
        });
    }
}