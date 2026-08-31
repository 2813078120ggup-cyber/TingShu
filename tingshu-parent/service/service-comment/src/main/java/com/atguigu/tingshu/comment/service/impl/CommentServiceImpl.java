package com.atguigu.tingshu.comment.service.impl;

import com.atguigu.tingshu.comment.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl implements CommentService {

	@Autowired
	private MongoTemplate mongoTemplate;

}
