package com.atguigu.tingshu.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.tingshu.album.client.AlbumInfoFeignClient;
import com.atguigu.tingshu.album.client.CategoryFeignClient;
import com.atguigu.tingshu.album.client.TrackInfoFeignClient;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.atguigu.tingshu.search.service.ItemService;
import com.atguigu.tingshu.user.client.UserInfoFeignClient;
import com.atguigu.tingshu.user.client.UserListenProcessFeignClient;
import com.atguigu.tingshu.vo.album.AlbumStatVo;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class ItemServiceImpl implements ItemService {
    
    // 根据专辑Id 获取详情数据
    @Autowired
    private AlbumInfoFeignClient albumInfoFeignClient;
    
    @Autowired
    private TrackInfoFeignClient trackInfoFeignClient;
    
    @Autowired
    private CategoryFeignClient categoryFeignClient;
    
    @Autowired
    private UserInfoFeignClient userInfoFeignClient;
    
    @Autowired
    private UserListenProcessFeignClient userListenProcessFeignClient;
    
    //@Autowired
    //private RedissonClient redissonClient;
    
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    
    //@GuiguCache
    @Override
    public Map<String, Object> getItem(Long albumId) {
        // 创建map集合对象
        Map<String, Object> result = new HashMap<>();
        
        // 1. 根据专辑id获取专辑信息
        // 通过albumId 查询albumInfo
        CompletableFuture<AlbumInfo> albumCompletableFuture = CompletableFuture.supplyAsync(() -> {
            Result<AlbumInfo> albumInfoResult = albumInfoFeignClient.getAlbumInfo(albumId);
            Assert.notNull(albumInfoResult);
            AlbumInfo albumInfo = albumInfoResult.getData();
            // 保存albumInfo
            result.put("albumInfo", albumInfo);
            Assert.notNull(albumInfo, "专辑信息为空");
            log.info("albumInfo:{}", JSON.toJSONString(albumInfo));
            return albumInfo;
        }, threadPoolExecutor);
        
        // 2. 根据专辑id获取四个统计数据
        CompletableFuture<Void> albumStatCompletableFuture = CompletableFuture.runAsync(() -> {
            Result<AlbumStatVo> albumStatVoResult = albumInfoFeignClient.getAlbumStatVo(albumId);
            AlbumStatVo albumStatVo = albumStatVoResult.getData();
            Assert.notNull(albumStatVo, "专辑统计信息为空");
            result.put("albumStatVo", albumStatVo);
            log.info("albumStatVo:{}", JSON.toJSONString(albumStatVo));
        }, threadPoolExecutor);
        
         // 3. 根据专辑里面三级id获取一级和二级分类数据
        CompletableFuture<Void> baseCategoryViewCompletableFuture = albumCompletableFuture.thenAcceptAsync(albumInfo -> {
            Result<BaseCategoryView> baseCategoryViewResult = categoryFeignClient.getCategoryView(albumInfo.getCategory3Id());
            BaseCategoryView baseCategoryView = baseCategoryViewResult.getData();
            result.put("baseCategoryView", baseCategoryView);
            Assert.notNull(baseCategoryView, "分类信息为空");
            log.info("baseCategoryView:{}", JSON.toJSONString(baseCategoryView));
        }, threadPoolExecutor);
        
        // 4. 根据用户id获取用户信息
        CompletableFuture<Void> announcerCompletableFuture = albumCompletableFuture.thenAcceptAsync(albumInfo -> {
            Result<UserInfoVo> userInfoVoResult = userInfoFeignClient.getUserInfoVo(albumInfo.getUserId());
            UserInfoVo userInfoVo = userInfoVoResult.getData();
            result.put("announcer", userInfoVo);
            Assert.notNull(userInfoVo, "用户信息为空");
            log.info("announcer:{}", JSON.toJSONString(userInfoVo));
        }, threadPoolExecutor);
        
        // 5. 多个任务都执行完汇总
        CompletableFuture.allOf(albumCompletableFuture, albumStatCompletableFuture, baseCategoryViewCompletableFuture, announcerCompletableFuture).join();
        // 返回map集合
        return result;
    }
}
