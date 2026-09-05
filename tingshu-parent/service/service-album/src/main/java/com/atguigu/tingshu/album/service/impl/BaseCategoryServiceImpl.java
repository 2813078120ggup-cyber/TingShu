package com.atguigu.tingshu.album.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.mapper.*;
import com.atguigu.tingshu.album.service.BaseCategoryService;
import com.atguigu.tingshu.model.album.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@SuppressWarnings({"all"})
public class BaseCategoryServiceImpl extends ServiceImpl<BaseCategory1Mapper, BaseCategory1> implements BaseCategoryService {
    
    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;
    
    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;
    
    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;
    
    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;
    
    @Autowired
    private BaseAttributeMapper baseAttributeMapper;
    
    //查询所有分类
    @Override
    public List<JSONObject> getBaseCategoryList() {
        //1 把视图里面所有分类查询出来，返回list集合
        List<BaseCategoryView> baseCategoryViewList =
                baseCategoryViewMapper.selectList(null);
        
        //2 创建list集合封装最终数据
        List<JSONObject> finalList = new ArrayList<>();
        
        //3 封装所有一级分类
        //把查询视图里面所有分类集合进行遍历，得到一级分类id，根据一级分类id进行分组
        //分组之后返回map集合
        // map的key：分组字段，一级分类id
        // map的value：每组里面所有数据集合
        Map<Long, List<BaseCategoryView>> map =
                baseCategoryViewList.stream()
                        .collect(Collectors
                                .groupingBy(BaseCategoryView::getCategory1Id));
        //for (Map.Entry<Long, List<BaseCategoryView>> entry : map.entrySet())
        //遍历map，
        map.forEach((k, v) -> {
            Long categoryId1 = k; //一级分类id
            List<BaseCategoryView> baseCategoryViewList1 = v; //每组一级分类组所有数据集合
            
            //封装一级分类
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("categoryId", categoryId1);
            jsonObject1.put("categoryName", baseCategoryViewList1.get(0).getCategory1Name());
            
            //封装二级分类
            //一级分类分组之后，每组里面集合，根据二级分类id分组
            //一级分类每组集合 v  == baseCategoryViewList1
            Map<Long, List<BaseCategoryView>> map1 =
                    baseCategoryViewList1.stream()
                            .collect(Collectors
                                    .groupingBy(BaseCategoryView::getCategory2Id));
            
            //创建list集合，用于每个一级分类所有二级分类集合
            List<JSONObject> categoryChild2 = new ArrayList<>();
            
            map1.forEach((k1, v1) -> {
                Long categoryId2 = k1; //二级分类id
                List<BaseCategoryView> baseCategoryViewList2 = v1;
                
                JSONObject jsonObject2 = new JSONObject();
                jsonObject2.put("categoryId", categoryId2);
                jsonObject2.put("categoryName",
                        baseCategoryViewList2.get(0).getCategory2Name());
                
                // baseCategoryViewList2 = v1有二级下面所有三级分类
                // List<BaseCategoryView> --> List<JSONObject>
                List<JSONObject> categoryChild3 =
                        baseCategoryViewList2.stream().map(baseCategoryView -> {
                            JSONObject jsonObject3 = new JSONObject();
                            jsonObject3.put("categoryId", baseCategoryView.getCategory3Id());
                            jsonObject3.put("categoryName", baseCategoryView.getCategory3Name());
                            return jsonObject3;
                        }).collect(Collectors.toList());
                
                //把三级集合放到每个二级分类对象里面
                jsonObject2.put("categoryChild", categoryChild3);
                
                //	将二级分类对象添加到集合中
                categoryChild2.add(jsonObject2);
            });
            
            //把二级分类集合放到每个一级里面
            jsonObject1.put("categoryChild", categoryChild2);
            
            //把所有一级分类对象放到最终集合里面
            finalList.add(jsonObject1);
        });
        
        return finalList;
    }
    
    
    @Override
    public List<BaseAttribute> findAttribute(Long category1Id) {
        // 调用mapper
        return baseAttributeMapper.selectAttribute(category1Id);
    }
    
    // 根据3级分类id获得2级和1级id
    
    @Override
    public BaseCategoryView getCategoryViewByCategory3Id(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }
    
    
    // 根据一级分类id查询置顶频道页的三级分类列表
    @Override
    public List<BaseCategory3> findTopBaseCategory3ByCategory1Id(Long category1Id) {
        //	select * from base_category3 where base_category3.category2_id in (101,102,103) and is_top = 1 limit 7;
        //	先根据一级分类Id 找到二级分类集合
        LambdaQueryWrapper<BaseCategory2> baseCategory2LambdaQueryWrapper = new LambdaQueryWrapper<>();
        baseCategory2LambdaQueryWrapper.eq(BaseCategory2::getCategory1Id, category1Id);
        List<BaseCategory2> baseCategory2List = baseCategory2Mapper.selectList(baseCategory2LambdaQueryWrapper);
        List<Long> category2IdList = baseCategory2List.stream().map(BaseCategory2::getId).collect(Collectors.toList());
        //	查询置顶消息，每页显示7条数据；
        LambdaQueryWrapper<BaseCategory3> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(BaseCategory3::getCategory2Id, category2IdList).eq(BaseCategory3::getIsTop, 1).last(" limit 7");
        return baseCategory3Mapper.selectList(wrapper);
    }
}
