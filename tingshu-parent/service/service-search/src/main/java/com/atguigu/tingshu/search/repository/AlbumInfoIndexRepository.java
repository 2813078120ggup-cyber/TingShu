package com.atguigu.tingshu.search.repository;

import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @className: AlbumInfoIndexRepository
 * @author: gc
 * @date: 2026/9/4 15:07
 * @description:
 */
public interface AlbumInfoIndexRepository extends ElasticsearchRepository<AlbumInfoIndex, Long> {


}
