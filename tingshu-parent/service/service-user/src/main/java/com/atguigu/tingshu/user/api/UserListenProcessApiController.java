package com.atguigu.tingshu.user.api;

import com.atguigu.tingshu.common.login.TingShuLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.user.service.UserListenProcessService;
import com.atguigu.tingshu.vo.user.UserListenProcessVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "用户声音播放进度管理接口")
@RestController
@RequestMapping("api/user/userListenProcess")
@SuppressWarnings({"all"})
public class UserListenProcessApiController {
    
    @Autowired
    private UserListenProcessService userListenProcessService;
    
    // 保存声音播放进度（时间）
    
    /**
     * 获取声音播放的时间
     *
     * @param trackId
     * @return
     */
    @TingShuLogin
    @Operation(summary = "获取声音的上次跳出时间")
    @GetMapping("/getTrackBreakSecond/{trackId}")
    public Result<BigDecimal> getTrackBreakSecond(@PathVariable Long trackId) {
        //	获取用户Id
        Long userId = AuthContextHolder.getUserId();
        //	调用服务层方法
        BigDecimal trackBreakSecond = userListenProcessService.getTrackBreakSecond(userId, trackId);
        //	返回数据
        return Result.ok(trackBreakSecond);
    }
    
    /**
     * 更新播放进度
     *
     * @param userListenProcessVo
     * @return
     */
    @TingShuLogin
    @Operation(summary = "更新播放进度")
    @PostMapping("/updateListenProcess")
    public Result updateListenProcess(@RequestBody UserListenProcessVo userListenProcessVo) {
        //	获取用户Id
        Long userId = AuthContextHolder.getUserId();
        //	调用服务层方法
        userListenProcessService.updateListenProcess(userId, userListenProcessVo);
        return Result.ok();
    }
}

