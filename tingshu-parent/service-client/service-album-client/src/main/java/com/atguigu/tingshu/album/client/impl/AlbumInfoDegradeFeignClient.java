package com.atguigu.tingshu.album.client.impl;


import com.atguigu.tingshu.album.client.AlbumInfoFeignClient;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AlbumInfoDegradeFeignClient implements AlbumInfoFeignClient {
    
    @Override
    public Result<AlbumInfo> getAlbumInfo(Long albumId) {
        return Result.fail();
    }
    
    
    @Override
    public Result<List<AlbumAttributeValue>> findAlbumAttributeValue(Long albumId) {
        return Result.fail();
    }
    
    
    @Override
    public Result getAlbumInfoStat(Long albumId) {
        return Result.fail();
    }
}
