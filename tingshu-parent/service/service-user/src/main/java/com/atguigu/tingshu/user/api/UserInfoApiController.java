package com.atguigu.tingshu.user.api;

import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.user.service.UserInfoService;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户管理接口")
@RestController
@RequestMapping("api/user/userInfo")
@SuppressWarnings({"all"})
public class UserInfoApiController {
    
    @Autowired
    private UserInfoService userInfoService;
    
    /**
     * 根据用户Id获取用户信息
     *
     * @param userId
     * @return
     */
    @Operation(summary = "根据用户id获取用户信息")
    @GetMapping("getUserInfoVo/{userId}")
    public Result<UserInfoVo> getUserInfoVo(@PathVariable Long userId) {
        // 获取用户信息
        UserInfo userInfo = userInfoService.getById(userId);
        if (userInfo == null) {
            return Result.fail();
        }
        // 创建UserInfoVo对象
        UserInfoVo userInfoVo = new UserInfoVo();
        // 复制属性
        BeanUtils.copyProperties(userInfo, userInfoVo);
        //  返回数据
        return Result.ok(userInfoVo);
    }
}

