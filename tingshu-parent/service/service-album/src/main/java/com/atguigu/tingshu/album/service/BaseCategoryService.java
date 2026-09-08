package com.atguigu.tingshu.album.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.model.album.BaseAttribute;
import com.atguigu.tingshu.model.album.BaseCategory1;
import com.atguigu.tingshu.model.album.BaseCategory3;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface BaseCategoryService extends IService<BaseCategory1> {

    //查询所有分类
    List<JSONObject> getBaseCategoryList();
    
    List<BaseAttribute> findAttribute(Long category1Id);
    
    // 根据3级分类id获得2级和1级id
    BaseCategoryView getCategoryViewByCategory3Id(Long category3Id);
    
    // 根据一级分类id查询置顶频道页的三级分类列表
    List<BaseCategory3> findTopBaseCategory3ByCategory1Id(Long category1Id);
    
    // 根据一级分类Id 获取全部数据
    JSONObject getAllCategoryList(Long category1Id);
    
    // 查询所有的一级分类数据
    List<BaseCategory1> findAllCategory1();
}
