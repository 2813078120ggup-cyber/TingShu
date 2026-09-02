package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.mapper.AlbumInfoMapper;
import com.atguigu.tingshu.album.mapper.TrackInfoMapper;
import com.atguigu.tingshu.album.mapper.TrackStatMapper;
import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.model.album.TrackStat;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.atguigu.tingshu.vo.album.TrackMediaInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
    @Autowired
    private AlbumInfoMapper albumInfoMapper;
    
    // 分页查询用户声音列表
    @Override
    public IPage<TrackListVo> findUserTrackPage(Page<TrackListVo> trackListVoPage, TrackInfoQuery trackInfoQuery) {
        //	调用mapper层方法
        return trackInfoMapper.selectUserTrackPage(trackListVoPage, trackInfoQuery);
    }
    
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
    
    
    // 删除声音
    
    @Override
    public void removeTrackInfo(Long trackId) {
        // 根据声音id获取专辑id
        TrackInfo trackInfo = trackInfoMapper.selectById(trackId);
        // 1. 根据声音id删除声音基本信息
        trackInfoMapper.deleteById(trackId);
        
        // 2. 修改声音所在专辑声音数量-1
        Long albumId = trackInfo.getAlbumId();
        // 根据专辑id查询专辑数据，把数量-1，进行更新
        AlbumInfo albumInfo = this.albumInfoService.getById(albumId);
        Integer infoIncludeTrackCount = albumInfo.getIncludeTrackCount();
        int includeTrackCount = infoIncludeTrackCount - 1;
        albumInfoMapper.updateById(albumInfo);
        
        // 3. 修改统计数据
        LambdaQueryWrapper<TrackStat> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TrackStat::getTrackId, trackId);
        trackStatMapper.delete(lambdaQueryWrapper);
        
        // 4. 删除腾讯云声音媒体
        trackInfoMapper.updateTrackNum(trackInfo.getAlbumId(), trackInfo.getOrderNum());
        //删除声音媒体
        vodService.removeTrack(trackInfo.getMediaFileId());
    }
    
    // 修改声音
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTrackInfo(Long id, TrackInfoVo trackInfoVo) {
        //	获取到声音对象
        TrackInfo trackInfo = this.getById(id);
        //    获取原始的fileId
        String mediaFileId = trackInfo.getMediaFileId();
        //	进行属性拷贝
        BeanUtils.copyProperties(trackInfoVo, trackInfo);
        //	获取声音信息 页面传递的fileId 与 数据库的 fileId 不相等就修改
        if (!trackInfoVo.getMediaFileId().equals(mediaFileId)) {
            //	说明已经修改过了.
            TrackMediaInfoVo trackMediaInfoVo = vodService.getTrackMediaInfo(trackInfoVo.getMediaFileId());
            //	判断对象不为空.
            if (null == trackMediaInfoVo) {
                //	抛出异常
                throw new GuiguException(ResultCodeEnum.VOD_FILE_ID_ERROR);
            }
            trackInfo.setMediaUrl(trackMediaInfoVo.getMediaUrl());
            trackInfo.setMediaType(trackMediaInfoVo.getType());
            trackInfo.setMediaDuration(trackMediaInfoVo.getDuration());
            trackInfo.setMediaSize(trackMediaInfoVo.getSize());
            // 删除云点播声音
            vodService.removeTrack(mediaFileId);
        }
        //	修改数据
        this.updateById(trackInfo);
    }
}
