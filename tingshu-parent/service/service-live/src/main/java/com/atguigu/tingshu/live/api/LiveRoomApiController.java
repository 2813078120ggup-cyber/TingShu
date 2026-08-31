package com.atguigu.tingshu.live.api;

import com.atguigu.tingshu.live.service.LiveRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/live/liveRoom")
@SuppressWarnings({"all"})
public class LiveRoomApiController {

	@Autowired
	private LiveRoomService liveRoomService;

}

