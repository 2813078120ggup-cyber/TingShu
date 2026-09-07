package com.atguigu.tingshu.album.mapper;

import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumTrackListVo;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TrackInfoMapper extends BaseMapper<TrackInfo> {
    
    // 分页查询用户声音列表
    IPage<TrackListVo> selectUserTrackPage(Page<TrackListVo> trackListVoPage, @Param("vo") TrackInfoQuery trackInfoQuery);
    
    // 更新声音播放次数
    void updateTrackNum(Long albumId, Integer orderNum);
    
    // 分页查询专辑声音列表
    IPage<AlbumTrackListVo> selectAlbumTrackPage(Page<AlbumTrackListVo> pageParam, Long albumId);
    
    // 更新声音播放量
    void updateStat(Long trackId, String statType, Integer count);
}
