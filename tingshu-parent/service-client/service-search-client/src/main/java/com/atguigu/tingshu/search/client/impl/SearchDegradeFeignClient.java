package com.atguigu.tingshu.search.client.impl;


import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.search.client.SearchFeignClient;
import org.springframework.stereotype.Component;

@Component
public class SearchDegradeFeignClient implements SearchFeignClient {
    
    @Override
    public Result updateLatelyAlbumRanking() {
        return null;
    }
}
