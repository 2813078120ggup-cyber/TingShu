package com.atguigu.tingshu.search.service;

import com.atguigu.tingshu.query.search.AlbumIndexQuery;
import com.atguigu.tingshu.vo.search.AlbumSearchResponseVo;

public interface SearchService {
    
    // 根据专辑id实现上架
    void upperAlbum(Long albumId);
    
    // 根据专辑id实现下架
    void lowerAlbum(Long albumId);
    
    // 根据关键词检索
    AlbumSearchResponseVo search(AlbumIndexQuery albumIndexQuery);
}
