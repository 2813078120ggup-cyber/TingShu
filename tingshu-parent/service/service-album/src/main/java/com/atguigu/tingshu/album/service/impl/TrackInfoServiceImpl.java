package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.mapper.AlbumInfoMapper;
import com.atguigu.tingshu.album.mapper.TrackInfoMapper;
import com.atguigu.tingshu.album.mapper.TrackStatMapper;
import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.model.album.TrackStat;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.user.client.UserInfoFeignClient;
import com.atguigu.tingshu.vo.album.AlbumTrackListVo;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.atguigu.tingshu.vo.album.TrackMediaInfoVo;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Autowired
    private UserInfoFeignClient userInfoFeignClient;
    
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
    
    // 查询专辑声音分页列表
    /*    查询声音分页
        ↓
    查询专辑信息
        ↓
    判断是否登录
       /        \
    未登录      已登录
      ↓           ↓
    是否免费    判断专辑付费类型
      ↓           ↓
    非免费      是否需要付费
      ↓           ↓
    过滤试听集   过滤试听集
      ↓           ↓
    标记付费    查询用户购买记录
                  ↓
              设置付费标识
        \          /
        返回分页结果*/
    
    @Override
    public IPage<AlbumTrackListVo> findAlbumTrackPage(Page<AlbumTrackListVo> pageParam, Long albumId, Long userId) {
        //	根据专辑Id 获取到声音集合
        IPage<AlbumTrackListVo> pageInfo = trackInfoMapper.selectAlbumTrackPage(pageParam, albumId);
        AlbumInfo albumInfo = albumInfoService.getById(albumId);
        Assert.notNull(albumInfo, "专辑对象不能为空");
        
        //	判断用户是否需要付费：0101-免费 0102-vip付费 0103-付费
        
        // 1. 根据userId判断当前是否登录
        // userId == null: 未登录
        if (null == userId) {
            // 1.1 如果没有登录：非免费专辑有试看，其他付费  isShowPaidMark=false 免费
            if (!SystemConstant.ALBUM_PAY_TYPE_FREE.equals(albumInfo.getPayType())) {
                // 获取需要付费的声音列表（当前声音序号大于试听声音序号）
                List<AlbumTrackListVo> albumTrackNeedPaidListVoList = pageInfo.getRecords()
                        .stream()
                        .filter(albumTrackListVo ->
                                albumTrackListVo.getOrderNum().intValue()
                                        >
                                        albumInfo.getTracksForFree())
                        .collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(albumTrackNeedPaidListVoList)) {
                    albumTrackNeedPaidListVoList.forEach(albumTrackListVo -> {
                        // 显示付费通知
                        albumTrackListVo.setIsShowPaidMark(true);
                    });
                }
            }
        } else {
            // 1.2 如果登录
            // 声明变量是否需要付费，默认免费
            boolean isNeedPaid = false;
            // VIP免费
            if (SystemConstant.ALBUM_PAY_TYPE_VIPFREE.equals(albumInfo.getPayType())) {
                // 获取用户信息
                Result<UserInfoVo> userInfoVoResult = userInfoFeignClient.getUserInfoVo(userId);
                Assert.notNull(userInfoVoResult, "用户信息不能为空");
                UserInfoVo userInfoVo = userInfoVoResult.getData();  // getData():从Result中取出业务数据
                // 1. VIP 免费,如果不是vip则需要付费，将isNeedPaid设置为true，需要购买
                if (userInfoVo.getIsVip().intValue() == 0) {
                    isNeedPaid = true;
                }
                // 1.1 如果是vip但是vip过期了（定时任务还为更新状态）
                if (userInfoVo.getIsVip().intValue() == 1 && userInfoVo.getVipExpireTime().before(new Date())) {
                    isNeedPaid = true;
                }
            } else if (SystemConstant.ALBUM_PAY_TYPE_REQUIRE.equals(albumInfo.getPayType())) {
                // 2. 付费
                isNeedPaid = true;
            }
            // 需要付费，判断用户是否购买过专辑或声音
            if (isNeedPaid) {
                // 处理试听声音，创建需要付费的声音列表
                List<AlbumTrackListVo> albumTrackNeedPaidListVoList = pageInfo.getRecords().stream()
                        .filter(albumTrackListVo ->
                                albumTrackListVo.getOrderNum().intValue() >
                                        albumInfo.getTracksForFree()).collect(Collectors.toList());
                // 判断
                if (!CollectionUtils.isEmpty(albumTrackNeedPaidListVoList)) {
                    // 判断用户是否购买该声音
                    // 获取到声音Id 集合列表  对象 -> id
                    List<Long> trackIdList = albumTrackNeedPaidListVoList.stream()
                            .map(AlbumTrackListVo::getTrackId).collect(Collectors.toList());
                    // 获取用户购买的声音列表
                    // userIsPaidTrack:判断用户是否购买声音列表
                    Result<Map<Long, Integer>> mapResult = userInfoFeignClient.userIsPaidTrack(albumId, trackIdList);
                    Assert.notNull(mapResult, "声音集合不能为空.");
                    Map<Long, Integer> map = mapResult.getData();
                    Assert.notNull(map, "map集合不能为空.");
                    albumTrackNeedPaidListVoList.forEach(albumTrackListVo -> {
                        // 如果map.get(albumTrackListVo.getTrackId()) == 1 已经购买过，则不显示付费标识;
                        // todo:为什么id为1为购买？
                        boolean isBuy = map.get(albumTrackListVo.getTrackId()) == 1 ? false : true;
                        albumTrackListVo.setIsShowPaidMark(isBuy);
                    });
                }
            }
        }
        // 返回集合数据
        return pageInfo;
    }
    
    // 更新声音播放量
    @Transactional
    @Override
    public void updateStat(Long albumId, Long trackId, String statType, Integer count) {
        //	更新声音播放量
        trackInfoMapper.updateStat(trackId, statType, count);
        //	更新专辑播放量
        if (statType.equals(SystemConstant.TRACK_STAT_PLAY)) {
            albumInfoService.updateStat(albumId, SystemConstant.ALBUM_STAT_PLAY, count);
        }
    }
}
