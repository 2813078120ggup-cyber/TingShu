package com.atguigu.tingshu.album.mapper;

import com.atguigu.tingshu.model.album.AlbumStat;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface AlbumStatMapper extends BaseMapper<AlbumStat> {
    
    
    Map<String, Object> getAlbumInfoStat(Long albumId);
}
