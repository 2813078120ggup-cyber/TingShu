package com.atguigu.tingshu.live.service.impl;

import com.atguigu.tingshu.live.mapper.LiveTagMapper;
import com.atguigu.tingshu.live.service.LiveTagService;
import com.atguigu.tingshu.model.live.LiveTag;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings({"all"})
public class LiveTagServiceImpl extends ServiceImpl<LiveTagMapper, LiveTag> implements LiveTagService {

	@Autowired
	private LiveTagMapper liveTagMapper;

}
