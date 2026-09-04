package com.atguigu.tingshu.album.client.impl;


import com.atguigu.tingshu.album.client.CategoryFeignClient;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import org.springframework.stereotype.Component;

@Component
public class CategoryDegradeFeignClient implements CategoryFeignClient {
    
    @Override
    public Result<BaseCategoryView> getCategoryView(Long category3Id) {
        return Result.fail();
    }
}
