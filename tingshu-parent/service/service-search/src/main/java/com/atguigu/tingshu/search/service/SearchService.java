package com.atguigu.tingshu.search.service;

import com.atguigu.tingshu.query.search.AlbumIndexQuery;
import com.atguigu.tingshu.vo.search.AlbumSearchResponseVo;

import java.util.List;
import java.util.Map;

public interface SearchService {
    
    // 根据专辑id实现上架
    void upperAlbum(Long albumId);
    
    // 根据专辑id实现下架
    void lowerAlbum(Long albumId);
    
    // 根据关键词检索
    AlbumSearchResponseVo search(AlbumIndexQuery albumIndexQuery);
    
    // 根据一级分类Id获取数据
    List<Map<String, Object>> channel(Long category1Id);
    
    // 自动补全
    List<String> completeSuggest(String keyword);
}
