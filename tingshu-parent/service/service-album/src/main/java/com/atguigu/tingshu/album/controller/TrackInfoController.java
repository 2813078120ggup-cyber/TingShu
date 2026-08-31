package com.atguigu.tingshu.album.controller;

import com.atguigu.tingshu.album.service.TrackInfoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "声音管理")
@RestController
@RequestMapping("admin/album/trackInfo")
@SuppressWarnings({"all"})
public class TrackInfoController {

	@Autowired
	private TrackInfoService trackInfoService;

}

