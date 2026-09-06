package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.mapper.AlbumAttributeValueMapper;
import com.atguigu.tingshu.album.mapper.AlbumInfoMapper;
import com.atguigu.tingshu.album.mapper.AlbumStatMapper;
import com.atguigu.tingshu.album.mapper.TrackInfoMapper;
import com.atguigu.tingshu.album.service.AlbumAttributeValueService;
import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.rabbit.constant.MqConst;
import com.atguigu.tingshu.common.rabbit.service.RabbitService;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.AlbumStat;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumAttributeValueVo;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.atguigu.tingshu.vo.album.AlbumStatVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Autowired
    private TrackInfoMapper trackInfoMapper;
    @Autowired
    private RabbitService rabbitService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
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
        
        // 判断专辑是否公开，如果公开，发送mq消息进行上架
        String isOpen = albumInfo.getIsOpen();
        if ("1".equals(isOpen)) {
            rabbitService.sendMessage(
                    MqConst.EXCHANGE_ALBUM,
                    MqConst.ROUTING_ALBUM_UPPER,
                    albumInfo.getId());
        }
    }
    
    // 保存专辑统计数据
    @Transactional(rollbackFor = Exception.class)
    public void saveAlbumStat(Long albumId, String statType) {
        AlbumStat albumStat = new AlbumStat();
        albumStat.setAlbumId(albumId);
        albumStat.setStatType(statType);  // 0401 0402 0403 0404
        albumStat.setStatNum(0);
        albumStatMapper.insert(albumStat);
    }
    
    
    // 批量添加方法
    @Autowired
    private AlbumAttributeValueService albumAttributeValueService;
    
    @Transactional(rollbackFor = Exception.class)
    public void saveAlbumInfo1(AlbumInfoVo albumInfoVo) {
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
        // List<AlbumAttributeValueVo> -> List<AlbumAttributeValue>
        List<AlbumAttributeValue> albumAttributeValueList = albumAttributeValueVoList.stream().map(albumAttributeValueVo -> {
            AlbumAttributeValue albumAttributeValue = new AlbumAttributeValue();
            BeanUtils.copyProperties(albumAttributeValueVo, albumAttributeValue);
            albumAttributeValue.setAlbumId(albumInfo.getId());
            return albumAttributeValue;
        }).collect(Collectors.toList());
        
        
        albumAttributeValueService.saveBatch(albumAttributeValueList);
        
        
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
    
    // 查询专辑列表
    @Override
    @Transactional(rollbackFor = Exception.class)
    public IPage<AlbumListVo> selectAlbumPage(Page<AlbumListVo> pageParam, AlbumInfoQuery albumInfoQuery) {
        return albumInfoMapper.selectUserAlbumPage(pageParam, albumInfoQuery);
    }
    
    // 根据用户id查询所有专辑列表
    // 默认查询前100个专辑
    @Override
    public List<AlbumInfo> findUserAllAlbumList(Long userId) {
        // 创建page对象，传递当前页和每页记录数
        Page<AlbumInfo> pageParam = new Page<>(1, 100);
        // 调用service方法
        // 根据userId查询
        LambdaQueryWrapper<AlbumInfo> wrapper = new LambdaQueryWrapper<>();
        // 只查id和title(指定查询字段)
        wrapper.select(AlbumInfo::getId, AlbumInfo::getAlbumTitle);
        wrapper.eq(AlbumInfo::getUserId, userId);
        // 排序 专辑id desc
        wrapper.orderByDesc(AlbumInfo::getId);
        IPage<AlbumInfo> albumInfoIPage = albumInfoMapper.selectPage(pageParam, wrapper);
        //从IPage对象中获取数据list集合
        List<AlbumInfo> list = albumInfoIPage.getRecords();
        return list;
    }
    
    // 删除专辑信息
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAlbumInfo(Long albumId) {
        // 1. 判断当前专辑下是否包含声音，如果包含不能删除
        // select count(*) from track_info where album_id =?
        LambdaQueryWrapper<TrackInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TrackInfo::getAlbumId, albumId);
        Long count = trackInfoMapper.selectCount(lambdaQueryWrapper);
        if (count > 0) {
            throw new GuiguException(400, "当前专辑下包含声音，不能删除");
        }
        
        // 2. 如果专辑下面不包含声音，可以删除
        // 2.1 删除专辑基本信息
        albumInfoMapper.deleteById(albumId);
        
        // 2.2 删除专辑标签名称和标签值数据
        LambdaQueryWrapper<AlbumAttributeValue> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        lambdaQueryWrapper1.eq(AlbumAttributeValue::getAlbumId, albumId);
        albumAttributeValueMapper.delete(lambdaQueryWrapper1);
        
        // 2.3 删除专辑四个统计数据
        LambdaQueryWrapper<AlbumStat> lambdaQueryWrapper2 = new LambdaQueryWrapper<>();
        lambdaQueryWrapper2.eq(AlbumStat::getAlbumId, albumId);
        albumStatMapper.delete(lambdaQueryWrapper2);
        
        // 发送mq消息
        rabbitService.sendMessage(
                MqConst.EXCHANGE_ALBUM,
                MqConst.ROUTING_ALBUM_LOWER,
                albumId);
    }
    
    // 根据id查询专辑信息
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AlbumInfo getAlbumInfo(Long albumId) {
        // 1. 根据专辑id获取专辑基本信息
        AlbumInfo albumInfo = albumInfoMapper.selectById(albumId);
        
        // 2. 根据专辑id获取标签数据
        LambdaQueryWrapper<AlbumAttributeValue> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AlbumAttributeValue::getAlbumId, albumId);
        List<AlbumAttributeValue> albumAttributeValueList = albumAttributeValueMapper.selectList(lambdaQueryWrapper);
        
        // 3. 把获取标签数据list集合封装到专辑对象里
        albumInfo.setAlbumAttributeValueVoList(albumAttributeValueList);
        return albumInfo;
    }
    
    // 修改专辑信息
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAlbumInfo(Long albumId, AlbumInfoVo albumInfoVo) {
        // 1. 根据专辑id修改专辑基本信息
        AlbumInfo albumInfo = albumInfoMapper.selectById(albumId);
        BeanUtils.copyProperties(albumInfoVo, albumInfo);
        albumInfoMapper.updateById(albumInfo);
        // 2. 根据专辑id删除标签名称和标签值数据
        LambdaQueryWrapper<AlbumAttributeValue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlbumAttributeValue::getAlbumId, albumId);
        albumAttributeValueMapper.delete(wrapper);
        // 3. 添加专辑下面标签名称和标签值数据
        List<AlbumAttributeValueVo> albumAttributeValueVoList = albumInfoVo.getAlbumAttributeValueVoList();
        if (!CollectionUtils.isEmpty(albumAttributeValueVoList)) {
            albumAttributeValueVoList.stream().forEach(albumAttributeValueVo -> {
                // albumAttributeValueVo -> albumAttributeValue
                AlbumAttributeValue albumAttributeValue = new AlbumAttributeValue();
                BeanUtils.copyProperties(albumAttributeValueVo, albumAttributeValue);
                // 单独设置专辑id
                albumAttributeValue.setAlbumId(albumId);
                albumAttributeValueMapper.insert(albumAttributeValue);
            });
        }
    }
    
    
    // 根据专辑id查询专辑统计信息
    
    @Override
    public Map<String, Object> getAlbumInfoStat(Long albumId) {
        return albumStatMapper.getAlbumInfoStat(albumId);
    }
    
    // 根据专辑id查询专辑标签数据
    
    @Override
    public List<AlbumAttributeValue> findAlbumAttributeValueByAlbumId(Long albumId) {
        LambdaQueryWrapper<AlbumAttributeValue> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AlbumAttributeValue::getAlbumId, albumId);
        List<AlbumAttributeValue> albumAttributeValueList = albumAttributeValueMapper.selectList(lambdaQueryWrapper);
        //	返回集合数据
        return albumAttributeValueList;
    }
    
    // 根据专辑Id 获取到统计信息
    @Override
    public AlbumStatVo getAlbumStatVoByAlbumId(Long albumId) {
        return albumInfoMapper.selectAlbumStat(albumId);
    }
}
