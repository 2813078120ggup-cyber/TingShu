package com.atguigu.tingshu.search.service;

import java.util.Map;

public interface ItemService {
    
    // 根据专辑Id 获取详情数据
    Map<String, Object> getItem(Long albumId);
}
