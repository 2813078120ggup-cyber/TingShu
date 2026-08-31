package com.atguigu.tingshu.album.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.service.BaseCategoryService;
import com.atguigu.tingshu.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 * 
 *
 */
@Tag(name = "分类管理")
@RestController
@RequestMapping(value="/admin/album/category")
@SuppressWarnings({"all"})
public class BaseCategoryController {
	
	@Autowired
	private BaseCategoryService baseCategoryService;


}

