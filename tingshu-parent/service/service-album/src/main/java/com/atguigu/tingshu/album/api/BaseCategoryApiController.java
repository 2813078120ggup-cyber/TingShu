package com.atguigu.tingshu.album.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.client.impl.CategoryDegradeFeignClient;
import com.atguigu.tingshu.album.service.BaseCategoryService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.BaseAttribute;
import com.atguigu.tingshu.model.album.BaseCategory1;
import com.atguigu.tingshu.model.album.BaseCategory3;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@Tag(name = "分类管理")
@RestController
@RequestMapping(value = "/api/album/category")
@SuppressWarnings({"all"})
public class BaseCategoryApiController {

//	routes:
//			- id: service-album
//			uri: lb://service-album
//			predicates:
//					- Path=/*/album/**
    
    @Autowired
    private BaseCategoryService baseCategoryService;
    
    //查询所有分类
    @GetMapping("getBaseCategoryList")
    public Result getBaseCategoryList() {
        //调用service方法
        //List<Map> list = baseCategoryService.getBaseCategoryList();
        
        List<JSONObject> list = baseCategoryService.getBaseCategoryList();
        
        return Result.ok(list);
    }
    
    
    // 根据id查询对应标签数据
    // /api/album/category/findAttribute/2
    @GetMapping("findAttribute/{category1Id}")
    public Result findAttribute(@PathVariable("category1Id") Long category1Id) {
        //调用service方法
        List<BaseAttribute> list = baseCategoryService.findAttribute(category1Id);
        return Result.ok(list);
    }
    
    // 根据3级id获得2级和1级id
    
    /**
     * 根据三级分类Id 获取到分类信息
     *
     * @param category3Id
     * @return
     */
    @Operation(summary = "通过三级分类id查询分类信息")
    @GetMapping("getCategoryView/{category3Id}")
    public Result<BaseCategoryView> getCategoryView(@PathVariable Long category3Id) {
        // 调用服务层方法
        BaseCategoryView baseCategoryView = baseCategoryService.getCategoryViewByCategory3Id(category3Id);
        return Result.ok(baseCategoryView);
    }
    
    // Request URL: http://localhost/api/album/category/findTopBaseCategory3/1
    //Request Method: GET
    
    /**
     * 根据一级分类Id 查询置顶频道页的三级分类列表
     *
     * @param category1Id
     * @return
     */
    @Operation(summary = "获取一级分类下置顶到频道页的三级分类列表")
    @GetMapping("findTopBaseCategory3/{category1Id}")
    public Result<List<BaseCategory3>> findTopBaseCategory3(@PathVariable Long category1Id) {
        //	获取三级分类列表
        List<BaseCategory3> baseCategory3List = baseCategoryService.findTopBaseCategory3ByCategory1Id(category1Id);
        //	返回数据
        return Result.ok(baseCategory3List);
    }
    
    
    //Request URL: http://localhost/api/album/category/getBaseCategoryList/1
    //Request Method: GET
    /**
     * 根据一级分类Id 获取全部数据
     *
     * @param category1Id
     * @return
     */
    @Operation(summary = "根据一级分类id获取全部分类信息")
    @GetMapping("getBaseCategoryList/{category1Id}")
    public Result<JSONObject> getBaseCategoryList(@PathVariable Long category1Id) {
        JSONObject jsonObject = baseCategoryService.getAllCategoryList(category1Id);
        return Result.ok(jsonObject);
    }
}

