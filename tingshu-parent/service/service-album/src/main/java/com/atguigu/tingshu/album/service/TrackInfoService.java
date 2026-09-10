package com.atguigu.tingshu.album.service;

import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumTrackListVo;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface TrackInfoService extends IService<TrackInfo> {
    
    // 保存声音
    void saveTrackInfo(TrackInfoVo trackInfoVo, Long userId);
    
    // 查询用户声音分页列表
    IPage<TrackListVo> findUserTrackPage(Page<TrackListVo> trackListVoPage, TrackInfoQuery trackInfoQuery);
    
    // 删除声音
    void removeTrackInfo(Long id);
    
    // 修改声音
    void updateTrackInfo(Long id, TrackInfoVo trackInfoVo);
    
    // 查询专辑声音分页列表
    IPage<AlbumTrackListVo> findAlbumTrackPage(Page<AlbumTrackListVo> pageParam, Long albumId, Long userId);
    
    // 更新声音播放量
    void updateStat(Long albumId, Long trackId, String statType, Integer count);
    
    // 获取可以购买声音集数
    List<Map<String, Object>> findUserTrackPaidList(Long trackId);
    
    // 批量获取下单付费声音列表
    List<TrackInfo> findPaidTrackInfoList(Long trackId, Integer trackCount);
}
