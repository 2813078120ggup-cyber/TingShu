package com.atguigu.tingshu.album.service;

import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AlbumInfoService extends IService<AlbumInfo> {
    
    // 保存专辑信息
    void saveAlbumInfo(AlbumInfoVo albumInfoVo);
    
    // 查询专辑列表
    IPage<AlbumListVo> selectAlbumPage(Page<AlbumListVo> pageParam, AlbumInfoQuery albumInfoQuery);
    
    // 删除专辑
    void removeAlbumInfo(Long albumId);
    
    // 根据专辑id获取专辑数据
    AlbumInfo getAlbumInfo(Long albumId);
    
    // 修改专辑信息
    void updateAlbumInfo(Long albumId, AlbumInfoVo albumInfoVo);
    
    // 根据用户id获取所有专辑列表
    List<AlbumInfo> findUserAllAlbumList(Long userId);
}
