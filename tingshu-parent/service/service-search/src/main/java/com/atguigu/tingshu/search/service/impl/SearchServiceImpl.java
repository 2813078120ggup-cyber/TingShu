package com.atguigu.tingshu.search.service.impl;

import com.atguigu.tingshu.album.client.AlbumInfoFeignClient;
import com.atguigu.tingshu.album.client.CategoryFeignClient;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import com.atguigu.tingshu.model.search.AttributeValueIndex;
import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.search.repository.AlbumInfoIndexRepository;
import com.atguigu.tingshu.search.service.SearchService;
import com.atguigu.tingshu.user.client.UserInfoFeignClient;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


@Slf4j
@Service
@SuppressWarnings({"all"})
public class SearchServiceImpl implements SearchService {
    
    @Autowired
    private AlbumInfoFeignClient albumInfoFeignClient;
    @Autowired
    private CategoryFeignClient categoryFeignClient;
    @Autowired
    private UserInfoFeignClient userInfoFeignClient;
    @Autowired
    private AlbumInfoIndexRepository albumInfoIndexRepository;
    
    // 根据专辑id实现上架
    /*@Override
    public void upperAlbum(Long albumId) {
        // 1. 调用5个远程调用接口，得到返回数据
        // 1.1 根据专辑id获取专辑信息
        Result<AlbumInfo> albumInfoResult = albumInfoFeignClient.getAlbumInfo(albumId);
        AlbumInfo albumInfo = albumInfoResult.getData();
        
        *//*if (albumInfo == null) {
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }*//*
        Assert.notNull(albumInfo, "专辑为空");  // 断言
        
        // 1.2 从专辑信息获取三级分类id，根据三级分类id获取一级和二级分类数据
        Long category3Id = albumInfo.getCategory3Id();
        Result<BaseCategoryView> categoryViewResult = categoryFeignClient.getCategoryView(category3Id);
        BaseCategoryView baseCategoryView = categoryViewResult.getData();
        Assert.notNull(baseCategoryView, "分类为空");  // 断言
        
        // 1.3 根据专辑id获取四个统计数据
        Result<Map<String, Object>> albumInfoStatResult = albumInfoFeignClient.getAlbumInfoStat(albumId);
        Map<String, Object> map = albumInfoStatResult.getData();
        //Integer playStatNum  = (Integer)map.get("playStatNum");
        
        // 1.4 根据专辑id获取标签名称和标签值数据列表
        Result<List<AlbumAttributeValue>> albumAttributeValueResult = albumInfoFeignClient.findAlbumAttributeValue(albumId);
        List<AlbumAttributeValue> albumAttributeValues = albumAttributeValueResult.getData();
        Assert.notNull(albumAttributeValues, "标签为空");  // 断言
        
        
        // 1.5 从专辑信息获取userId，根据userId获取用户信息
        Long userId = albumInfo.getUserId();
        Result<UserInfoVo> userInfoResult = userInfoFeignClient.getUserInfoVo(userId);
        UserInfoVo userInfo = userInfoResult.getData();
        Assert.notNull(userInfo, "用户为空");  // 断言
        
        
        // 2. 将五个远程调用接口获取数据封装到AlbumInfoIndex实体类
        AlbumInfoIndex albumInfoIndex = new AlbumInfoIndex();
        // 先封装albumInfo
        BeanUtils.copyProperties(albumInfo, albumInfoIndex);
        // 封装baseCategoryView的一级、二级、三级分类
        Long category1Id = baseCategoryView.getCategory1Id();
        Long category2Id = baseCategoryView.getCategory2Id();
        albumInfoIndex.setCategory1Id(category1Id);
        albumInfoIndex.setCategory2Id(category2Id);
        albumInfoIndex.setCategory3Id(category3Id);
        // 封装统计信息4
        // TODO: 为了测试方便采用随机数，实际开发中应该根据业务逻辑生成统计信息
        int num1 = new Random().nextInt(1000);
        int num2 = new Random().nextInt(100);
        int num3 = new Random().nextInt(50);
        int num4 = new Random().nextInt(300);
        albumInfoIndex.setPlayStatNum(num1);
        albumInfoIndex.setSubscribeStatNum(num2);
        albumInfoIndex.setBuyStatNum(num3);
        albumInfoIndex.setCommentStatNum(num4);
        double hotScore = num1 * 0.2 + num2 * 0.3 + num3 * 0.4 + num4 * 0.1;
        // 设置热度排名
        albumInfoIndex.setHotScore(hotScore);
        // 封装标签名称和标签值 List<AlbumAttributeValue> albumAttributeValues
        // albumAttributeValues -> List<AttributeValueIndex>
        if (!CollectionUtils.isEmpty(albumAttributeValues)) {
            List<AttributeValueIndex> attributeValueIndexList = albumAttributeValues.stream().map(item -> {
                AttributeValueIndex attributeValueIndex = new AttributeValueIndex();
                BeanUtils.copyProperties(item, attributeValueIndex);
                return attributeValueIndex;
            }).toList();
            albumInfoIndex.setAttributeValueIndexList(attributeValueIndexList);
        }
        
        // 封装用户昵称
        albumInfoIndex.setAnnouncerName(userInfo.getNickname());
        
        
        // 3. 调用AlbumInfoIndexRepository的save方法实现添加
        albumInfoIndexRepository.save(albumInfoIndex);
        
    }*/
    
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    
    // 线程池并行上传
    @Override
    public void upperAlbum(Long albumId) {
        AlbumInfoIndex albumInfoIndex = new AlbumInfoIndex();
        
        // 根据专辑id获取专辑信息
        CompletableFuture<AlbumInfo> completableFuture1 =
                CompletableFuture.supplyAsync(() -> {
                    Result<AlbumInfo> albumInfoResult = albumInfoFeignClient.getAlbumInfo(albumId);
                    AlbumInfo albumInfo = albumInfoResult.getData();
                    Assert.notNull(albumInfo, "专辑为空");
                    
                    // 封装到albumINfoIndex
                    BeanUtils.copyProperties(albumInfo, albumInfoIndex);
                    return albumInfo;
                }, threadPoolExecutor);
        
        // 从专辑信息获取三级分类id，根据三级分类id获取一级和二级分类数据
        // 在获取专辑信息后执行
        CompletableFuture<Void> completableFuture2 =
                completableFuture1.thenAcceptAsync((albumInfo) -> {
                    // 获取3级分类id
                    Long category3Id = albumInfo.getCategory3Id();
                    Result<BaseCategoryView> categoryViewResult = categoryFeignClient.getCategoryView(category3Id);
                    BaseCategoryView baseCategoryView = categoryViewResult.getData();
                    Assert.notNull(baseCategoryView, "分类为空");
                    
                    // 封装到albumInfoIndex
                    Long category1Id = baseCategoryView.getCategory1Id();
                    Long category2Id = baseCategoryView.getCategory2Id();
                    albumInfoIndex.setCategory1Id(category1Id);
                    albumInfoIndex.setCategory2Id(category2Id);
                    albumInfoIndex.setCategory3Id(category3Id);
                    
                }, threadPoolExecutor);
        
        // 根据专辑id获取四个统计数据
        
        
        // 获取专辑标签数据
        CompletableFuture<Void> completableFuture3 = CompletableFuture.runAsync(() -> {
            Result<List<AlbumAttributeValue>> albumAttributeValueResult = albumInfoFeignClient.findAlbumAttributeValue(albumId);
            
            List<AlbumAttributeValue> albumAttributeValues = albumAttributeValueResult.getData();
            if (!CollectionUtils.isEmpty(albumAttributeValues)) {
                List<AttributeValueIndex> attributeValueIndexList = albumAttributeValues.stream().map(item -> {
                    AttributeValueIndex attributeValueIndex = new AttributeValueIndex();
                    BeanUtils.copyProperties(item, attributeValueIndex);
                    return attributeValueIndex;
                }).toList();
                albumInfoIndex.setAttributeValueIndexList(attributeValueIndexList);
            }
            Assert.notNull(albumAttributeValues, "标签为空");
        }, threadPoolExecutor);
        
        // 获取用户信息
        CompletableFuture<Void> completableFuture4 = completableFuture1.thenAcceptAsync(albumInfo -> {
            Long userId = albumInfo.getUserId();
            log.info("获取用户信息, albumId: {}, userId: {}", albumId, userId);
            Result<UserInfoVo> userInfoResult = userInfoFeignClient.getUserInfoVo(userId);
            log.info("用户信息返回: code={}, message={}, data={}", userInfoResult.getCode(), userInfoResult.getMessage(), userInfoResult.getData());
            UserInfoVo userInfo = userInfoResult.getData();
            Assert.notNull(userInfo, "用户为空, userId=" + userId);
            
            // 封装到albumInfoIndex
            albumInfoIndex.setAnnouncerName(userInfo.getNickname());
        }, threadPoolExecutor);
        
        //  赋值初始化统计信息：
        int playStatNum = new Random().nextInt(100000);
        int subscribeStatNum = new Random().nextInt(100000000);
        int buyStatNum = new Random().nextInt(10000000);
        int commentStatNum = new Random().nextInt(1000000000);
        albumInfoIndex.setPlayStatNum(playStatNum);
        albumInfoIndex.setSubscribeStatNum(subscribeStatNum);
        albumInfoIndex.setBuyStatNum(buyStatNum);
        albumInfoIndex.setCommentStatNum(commentStatNum);
        
        // 等待所有 CompletableFuture 执行完成
        CompletableFuture.allOf(completableFuture1, completableFuture2, completableFuture3, completableFuture4).join();
        
        // 调用方法添加到es
        albumInfoIndexRepository.save(albumInfoIndex);
    }
    
    // 根据专辑id实现下架
    @Override
    public void lowerAlbum(Long albumId) {
        albumInfoIndexRepository.deleteById(albumId);
    }
}
