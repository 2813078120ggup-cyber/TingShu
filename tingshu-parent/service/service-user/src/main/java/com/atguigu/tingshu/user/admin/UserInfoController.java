package com.atguigu.tingshu.user.admin;

import com.atguigu.tingshu.user.service.UserInfoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户管理接口")
@RestController
@RequestMapping("admin/user/userInfo")
@SuppressWarnings({"all"})
public class UserInfoController {

	@Autowired
	private UserInfoService userInfoService;


}

