package com.atguigu.tingshu.album.controller;

import com.atguigu.tingshu.album.service.AlbumInfoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "专辑管理")
@RestController
@RequestMapping("admin/album/albumInfo")
@SuppressWarnings({"all"})
public class AlbumInfoController {

	@Autowired
	private AlbumInfoService albumInfoService;

}

