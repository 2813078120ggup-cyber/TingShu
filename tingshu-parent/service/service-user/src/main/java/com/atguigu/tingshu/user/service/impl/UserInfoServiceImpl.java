package com.atguigu.tingshu.user.service.impl;

import com.atguigu.tingshu.album.client.TrackInfoFeignClient;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.model.user.*;
import com.atguigu.tingshu.user.mapper.*;
import com.atguigu.tingshu.user.service.UserInfoService;
import com.atguigu.tingshu.user.service.UserPaidTrackService;
import com.atguigu.tingshu.user.strategy.ItemTypeStrategy;
import com.atguigu.tingshu.user.strategy.StrategyFactory;
import com.atguigu.tingshu.vo.user.UserPaidRecordVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
    
    @Autowired
    private UserInfoMapper userInfoMapper;
    
    @Autowired
    private UserPaidAlbumMapper userPaidAlbumMapper;
    
    @Autowired
    private UserPaidTrackService userPaidTrackService;
    
    @Autowired
    private UserPaidTrackMapper userPaidTrackMapper;
    
    @Autowired
    private TrackInfoFeignClient trackInfoFeignClient;
    
    @Autowired
    private VipServiceConfigMapper vipServiceConfigMapper;
    
    @Autowired
    private UserVipServiceMapper userVipServiceMapper;
    
    // 根据用户id查询用户是否购买过声音列表
    /*    传入：
    userId + albumId + trackIdList
            ↓
    先查是否购买整张专辑
            ↓
          买过？
         /     \
       是       否
       ↓         ↓
    所有声音=1  查询单集购买记录
       ↓         ↓
    返回Map    获取已购买trackId
                  ↓
              遍历所有trackId
              /           \
           买过           没买
            ↓              ↓
            1              0
              \           /
                 返回Map*/
    @Override
    public Map<Long, Integer> userIsPaidTrack(Long userId, Long albumId, List<Long> trackIdList) {
        //	1. 根据用户id和专辑id查询用户是否购买专辑 user_paid_album
        LambdaQueryWrapper<UserPaidAlbum> userPaidAlbumLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userPaidAlbumLambdaQueryWrapper.eq(UserPaidAlbum::getUserId, userId).eq(UserPaidAlbum::getAlbumId, albumId);
        UserPaidAlbum userPaidAlbum = userPaidAlbumMapper.selectOne(userPaidAlbumLambdaQueryWrapper);
        //	如果用户购买过专辑，则专辑里面所有声音都被购买 map的key为声音Id，value为1
        if (null != userPaidAlbum) {
            //	创建一个map 集合
            HashMap<Long, Integer> map = new HashMap<>();
            //	如果查询到对应的专辑购买记录，则默认将声音Id 赋值为 1
            trackIdList.forEach(trackId -> {
                map.put(trackId, 1);
            });
            return map;
        } else {
            // 3. 如果用户没有买过专辑，根据用户id+声音id查询用户购买了哪些声音 user_paid_track
            LambdaQueryWrapper<UserPaidTrack> userPaidTrackLambdaQueryWrapper = new LambdaQueryWrapper<>();
            // in UserPaidTrack:用户购买的声音id集合
            userPaidTrackLambdaQueryWrapper.eq(UserPaidTrack::getUserId, userId).in(UserPaidTrack::getTrackId, trackIdList);
            // UserPaidTrack集合
            List<UserPaidTrack> userPaidTrackList = userPaidTrackService.list(userPaidTrackLambdaQueryWrapper);
            // 获取到用户购买声音Id 集合
            List<Long> userPaidTrackIdList = userPaidTrackList.stream()
                    .map(UserPaidTrack::getTrackId).collect(Collectors.toList());
            // 创建一个map 集合
            HashMap<Long, Integer> map = new HashMap<>();
            trackIdList.forEach(trackId -> {
                if (userPaidTrackIdList.contains(trackId)) {
                    //	用户已购买声音
                    map.put(trackId, 1);
                } else {
                    map.put(trackId, 0);
                }
            });
            return map;
        }
    }
    
    
    @Override
    public Boolean isPaidAlbum(Long userId, Long albumId) {
        // 根据用户Id 与专辑Id 查询是否有记录
        LambdaQueryWrapper<UserPaidAlbum> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserPaidAlbum::getUserId, userId).eq(UserPaidAlbum::getAlbumId, albumId);
        Long count = userPaidAlbumMapper.selectCount(lambdaQueryWrapper);
        return count > 0;
    }
    
    // 根据专辑id+用户id获取购买的声音id列表
    
    @Override
    public List<Long> findUserPaidTrackList(Long userId, Long albumId) {
        // 根据用户Id 与 专辑Id 获取到已购买的声音集合
        LambdaQueryWrapper<UserPaidTrack> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserPaidTrack::getUserId, userId)
                .eq(UserPaidTrack::getAlbumId, albumId);
        List<UserPaidTrack> userPaidTrackList = userPaidTrackMapper.selectList(lambdaQueryWrapper);
        // 获取到已购买的声音集合Id 列表
        List<Long> trackIdList = userPaidTrackList.stream().map(UserPaidTrack::getTrackId).collect(Collectors.toList());
        // 返回集合数据
        return trackIdList;
    }
    
    //添加购买记录
    // 没有策略模式方法
    
    @Override
    public void savePaidRecord(UserPaidRecordVo userPaidRecordVo) {
        // 根据购买类型判断向哪张表插入数据.
        // 获取购买类型
        String itemType = userPaidRecordVo.getItemType();
        // 1001:专辑
        if (itemType.equals(SystemConstant.ORDER_ITEM_TYPE_ALBUM)) {
            //  专辑 user_paid_album
            //  防止重复添加.用户之前可能购买过
            LambdaQueryWrapper<UserPaidAlbum> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserPaidAlbum::getUserId, userPaidRecordVo.getUserId());
            //  wrapper.eq(UserPaidAlbum::getAlbumId,userPaidRecordVo.getItemIdList().get(0));
            wrapper.eq(UserPaidAlbum::getOrderNo, userPaidRecordVo.getOrderNo());
            UserPaidAlbum userPaidAlbum = userPaidAlbumMapper.selectOne(wrapper);
            if (null != userPaidAlbum) {
                return;
            }
            //  新增：
            userPaidAlbum = new UserPaidAlbum();
            // 用户id
            userPaidAlbum.setUserId(userPaidRecordVo.getUserId());
            // 订单号
            userPaidAlbum.setOrderNo(userPaidRecordVo.getOrderNo());
            //  专辑Id 声音Id vip_service_config.id ==> order_detial.item_id;
            // 专辑Id
            userPaidAlbum.setAlbumId(userPaidRecordVo.getItemIdList().get(0));
            userPaidAlbumMapper.insert(userPaidAlbum);
        }
        // 1002:声音
        else if (userPaidRecordVo.getItemType().equals(SystemConstant.ORDER_ITEM_TYPE_TRACK)) {
            //  声音 user_paid_track防止重复添加
            LambdaQueryWrapper<UserPaidTrack> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserPaidTrack::getUserId, userPaidRecordVo.getUserId());
            wrapper.eq(UserPaidTrack::getOrderNo, userPaidRecordVo.getOrderNo());
            Long count = userPaidTrackMapper.selectCount(wrapper);
            if (count > 0) {
                return;
            }
            // 远程调用：获取专辑Id
            Result<TrackInfo> trackInfoResult = trackInfoFeignClient.getTrackInfo(userPaidRecordVo.getItemIdList().get(0));
            TrackInfo trackInfo = trackInfoResult.getData();
            //  声音Id 有可能一次购买 多个 .需要循环遍历
            userPaidRecordVo.getItemIdList().stream().forEach(trackId -> {
                //  保存数据：
                UserPaidTrack userPaidTrack = new UserPaidTrack();
                userPaidTrack.setTrackId(trackId);
                userPaidTrack.setOrderNo(userPaidRecordVo.getOrderNo());
                userPaidTrack.setUserId(userPaidRecordVo.getUserId());
                //  获取专辑Id
                userPaidTrack.setAlbumId(trackInfo.getAlbumId());
                userPaidTrackMapper.insert(userPaidTrack);
            });
        }
        // 1003:vip
        else {
            //  vip user_vip_service 保存： vip:续期
            //  获取用户对象信息.
            UserInfo userInfo = userInfoMapper.selectById(userPaidRecordVo.getUserId());
            // 当前时间
            Date currentTime = new Date();
            // 判断vip是否过期
            if (userInfo.getIsVip().intValue() == 1 && userInfo.getVipExpireTime().after(new Date())) {
                //  续期：
                currentTime = userInfo.getVipExpireTime();
            }
            // 根据vipid查询开通月份
            VipServiceConfig vipServiceConfig = vipServiceConfigMapper.selectById(userPaidRecordVo.getItemIdList().get(0));
            Integer serviceMonth = vipServiceConfig.getServiceMonth();
            //  LocalDate LocalDateTime LocalTime
            // 新过期时间
            Date expireTime = new LocalDateTime(currentTime).plusMonths(serviceMonth).toDate();
            //  创建对象
            UserVipService userVipService = new UserVipService();
            userVipService.setOrderNo(userPaidRecordVo.getOrderNo());
            userVipService.setUserId(userPaidRecordVo.getUserId());
            userVipService.setStartTime(new Date());
            //--------------> 购买的时长:
            userVipService.setExpireTime(expireTime);
            //  保存
            userVipServiceMapper.insert(userVipService);
            //  修改user_info.is_vip=1;
            userInfo.setIsVip(1);
            //  设置过期时间
            userInfo.setVipExpireTime(expireTime);
            //  手动模拟异常信息。
            //  int i = 1/0;
            //  修改
            this.userInfoMapper.updateById(userInfo);
        }
    }
    
    
    @Autowired
    private StrategyFactory strategyFactory;
    
    // 使用策略模式优化添加购买记录
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void userPayRecord(UserPaidRecordVo userPaidRecordVo) {
        String itemType = userPaidRecordVo.getItemType();
        //从策略工厂中获取对应的策略对象
        ItemTypeStrategy strategy = strategyFactory.getStrategy(itemType);
        //执行策略对象的任务
        strategy.savePaidRecord(userPaidRecordVo);
    }
    
    
}
