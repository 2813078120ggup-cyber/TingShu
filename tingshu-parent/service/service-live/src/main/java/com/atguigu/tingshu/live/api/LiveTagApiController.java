package com.atguigu.tingshu.live.api;

import com.atguigu.tingshu.live.service.LiveTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/live/liveTag")
@SuppressWarnings({"all"})
public class LiveTagApiController {

	@Autowired
	private LiveTagService liveTagService;

}

