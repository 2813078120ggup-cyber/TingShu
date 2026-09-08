package com.atguigu.tingshu.album.service;

import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.atguigu.tingshu.vo.album.AlbumStatVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface AlbumInfoService extends IService<AlbumInfo> {
    
    // 保存专辑信息
    void saveAlbumInfo(AlbumInfoVo albumInfoVo);
    
    // 查询专辑列表
    IPage<AlbumListVo> selectAlbumPage(Page<AlbumListVo> pageParam, AlbumInfoQuery albumInfoQuery);
    
    // 删除专辑
    void removeAlbumInfo(Long albumId);
    
    // 根据专辑id获取专辑数据
    AlbumInfo getAlbumInfo(Long albumId);
    
    // 修改：根据id查询专辑信息
    AlbumInfo getAlbumInfoById(Long albumId);
    
    // 使用Redisson
    AlbumInfo getAlbumInfoRedisson(Long albumId);
    
    // 修改专辑信息
    void updateAlbumInfo(Long albumId, AlbumInfoVo albumInfoVo);
    
    // 根据用户id获取所有专辑列表
    List<AlbumInfo> findUserAllAlbumList(Long userId);
    
    // 根据专辑id获取专辑统计数据
    Map<String, Object> getAlbumInfoStat(Long albumId);
    
    // 根据专辑id获取专辑属性值列表
    List<AlbumAttributeValue> findAlbumAttributeValueByAlbumId(Long albumId);
    
    // 根据专辑Id 获取到统计信息
    AlbumStatVo getAlbumStatVoByAlbumId(Long albumId);
    
    // 更新统计信息
    void updateStat(Long albumId, String albumStatPlay, Integer count);
}
