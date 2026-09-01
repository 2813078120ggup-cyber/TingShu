package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.mapper.AlbumAttributeValueMapper;
import com.atguigu.tingshu.album.mapper.AlbumInfoMapper;
import com.atguigu.tingshu.album.mapper.AlbumStatMapper;
import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.AlbumStat;
import com.atguigu.tingshu.vo.album.AlbumAttributeValueVo;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class AlbumInfoServiceImpl extends ServiceImpl<AlbumInfoMapper, AlbumInfo> implements AlbumInfoService {
    
    @Autowired
    private AlbumInfoMapper albumInfoMapper;
    @Autowired
    private AlbumAttributeValueMapper albumAttributeValueMapper;
    @Autowired
    private AlbumStatMapper albumStatMapper;
    
    @Override
    public void saveAlbumInfo(AlbumInfoVo albumInfoVo) {
        // 1. 添加专辑基本信息 album_info
        AlbumInfo albumInfo = new AlbumInfo();
        // albumInfoVo -> albumInfo
        BeanUtils.copyProperties(albumInfoVo, albumInfo);
        
        // 专辑有几个值需要单独设置，前端没有传递过来
        // TODO userId 用户id
        albumInfo.setUserId(1L);
        // 专辑状态：通过
        albumInfo.setStatus(SystemConstant.ALBUM_STATUS_PASS); // 0301
        // 设置首付专辑中免费试听集数
        String payType = albumInfo.getPayType();
        // 避免空指针异常 NullPointerException
        if (!SystemConstant.ALBUM_PAY_TYPE_FREE.equals(payType)) {
            albumInfo.setTracksForFree(3);
        }
        
        albumInfoMapper.insert(albumInfo);
        
        // 2. 添加专辑下面标签名称和标签值数据 album_attribute_value
        List<AlbumAttributeValueVo> albumAttributeValueVoList = albumInfoVo.getAlbumAttributeValueVoList();
        if (!CollectionUtils.isEmpty(albumAttributeValueVoList)) {
            albumAttributeValueVoList.stream().forEach(albumAttributeValueVo -> {
                // albumAttributeValueVo -> albumAttributeValue
                AlbumAttributeValue albumAttributeValue = new AlbumAttributeValue();
                BeanUtils.copyProperties(albumAttributeValueVo, albumAttributeValue);
                // 单独设置专辑id
                albumAttributeValue.setAlbumId(albumInfo.getId());
                albumAttributeValueMapper.insert(albumAttributeValue);
            });
        }
        
        //3. 添加转系统计数据 播放量，订阅量等 初始值0 album_stat
        // 播放量
        this.saveAlbumStat(albumInfo.getId(), SystemConstant.ALBUM_STAT_PLAY);
        // 订阅量
        this.saveAlbumStat(albumInfo.getId(), SystemConstant.ALBUM_STAT_SUBSCRIBE);
        // 浏览量
        this.saveAlbumStat(albumInfo.getId(), SystemConstant.ALBUM_STAT_BROWSE);
        // 评论数
        this.saveAlbumStat(albumInfo.getId(), SystemConstant.ALBUM_STAT_COMMENT);
        
        
    }
    // 保存专辑统计数据
    public void saveAlbumStat(Long albumId, String statType) {
        AlbumStat albumStat = new AlbumStat();
        albumStat.setAlbumId(albumId);
        albumStat.setStatType(statType);  // 0401 0402 0403 0404
        albumStat.setStatNum(0);
        albumStatMapper.insert(albumStat);
    }
    
    
    
}
