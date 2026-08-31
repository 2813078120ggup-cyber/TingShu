package com.atguigu.tingshu.comment.api;

import com.atguigu.tingshu.comment.service.CommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "专辑评论管理")
@RestController
@RequestMapping("/api/comment")
@Slf4j
public class CommentApiController {

	@Autowired
	private CommentService commentService;

}

