package com.atguigu.tingshu.album.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.service.BaseCategoryService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.BaseAttribute;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@Tag(name = "分类管理")
@RestController
@RequestMapping(value="/api/album/category")
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
	public Result getBaseCategoryList(){
		//调用service方法
		//List<Map> list = baseCategoryService.getBaseCategoryList();

		List<JSONObject> list = baseCategoryService.getBaseCategoryList();

		return Result.ok(list);
	}
}

