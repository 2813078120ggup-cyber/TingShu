package com.atguigu.tingshu.live.service.impl;

import com.atguigu.tingshu.live.mapper.LiveRoomMapper;
import com.atguigu.tingshu.live.service.LiveRoomService;
import com.atguigu.tingshu.model.live.LiveRoom;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings({"all"})
public class LiveRoomServiceImpl extends ServiceImpl<LiveRoomMapper, LiveRoom> implements LiveRoomService {

	@Autowired
	private LiveRoomMapper liveRoomMapper;

}
