package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.mapper.TrackInfoMapper;
import com.atguigu.tingshu.album.mapper.TrackStatMapper;
import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.model.album.TrackStat;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.atguigu.tingshu.vo.album.TrackMediaInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class TrackInfoServiceImpl extends ServiceImpl<TrackInfoMapper, TrackInfo> implements TrackInfoService {
    
    @Autowired
    private TrackInfoMapper trackInfoMapper;
    @Autowired
    private VodService vodService;
    @Autowired
    private AlbumInfoService albumInfoService;
    @Autowired
    private TrackStatMapper trackStatMapper;
    
    // 保存声音
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTrackInfo(TrackInfoVo trackInfoVo, Long userId) {
        // 1. 添加声音的基本信息 track_info
        TrackInfo trackInfo = new TrackInfo();
        BeanUtils.copyProperties(trackInfoVo, trackInfo);
        
        // 需要手动设置的参数
        // 用户id
        trackInfo.setUserId(1L);
        // 声音再专辑中的排序值
        // 获取专辑下最大order_num(最新)
        LambdaQueryWrapper<TrackInfo> queryWrapper = new LambdaQueryWrapper<>();
        // 设置查询字段order_num
        queryWrapper.select(TrackInfo::getOrderNum);
        // 条件：专辑id
        queryWrapper.eq(TrackInfo::getAlbumId, trackInfo.getAlbumId());
        // 排序：order_num 降序
        queryWrapper.orderByDesc(TrackInfo::getOrderNum);
        // 获取第一条数据
        queryWrapper.last(" limit 1");
        TrackInfo trackInfo_ordernum = trackInfoMapper.selectOne(queryWrapper);
        int orderNum = 1;
        if (null != trackInfo_ordernum) {
            orderNum = trackInfo_ordernum.getOrderNum() + 1;
        }
        // 数量+1
        trackInfo.setOrderNum(orderNum);
        
        
        // 声音的其他信息，如时长、大小、类型等，通过腾讯云查询
        // 根据声音的mediaFileId调用腾讯云方法获取
        TrackMediaInfoVo trackMediaInfoVo = vodService.getTrackMediaInfo(trackInfoVo.getMediaFileId());
        // 设置到trackInfo
        trackInfo.setMediaDuration(trackMediaInfoVo.getDuration());
        trackInfo.setMediaSize(trackMediaInfoVo.getSize());
        trackInfo.setMediaUrl(trackMediaInfoVo.getMediaUrl());
        trackInfo.setMediaType(trackMediaInfoVo.getType());
        // 调用方法添加
        trackInfoMapper.insert(trackInfo);
        
        // 3. 操作专辑表，修改专辑表里面声音数量+1
        AlbumInfo albumInfo = albumInfoService.getById(trackInfo.getAlbumId());
        int includeTrackCount = albumInfo.getIncludeTrackCount() + 1;
        albumInfo.setIncludeTrackCount(includeTrackCount);
        albumInfoService.updateById(albumInfo);
        
        
        // 2. 添加声音的统计数据，初始值0 track_stat
        this.saveTrackStat(trackInfo.getId(), SystemConstant.TRACK_STAT_PLAY);
        this.saveTrackStat(trackInfo.getId(), SystemConstant.TRACK_STAT_COLLECT);
        this.saveTrackStat(trackInfo.getId(), SystemConstant.TRACK_STAT_PRAISE);
        this.saveTrackStat(trackInfo.getId(), SystemConstant.TRACK_STAT_COMMENT);
        
    }
    
    // 2. 添加声音的统计数据，初始值0 track_stat
    
    /**
     * 初始化统计数量
     *
     * @param trackId
     * @param trackType
     */
    private void saveTrackStat(Long trackId, String trackType) {
        TrackStat trackStat = new TrackStat();
        trackStat.setTrackId(trackId);
        trackStat.setStatType(trackType);
        trackStat.setStatNum(0);
        this.trackStatMapper.insert(trackStat);
    }
}
