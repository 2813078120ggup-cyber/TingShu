package com.atguigu.tingshu.user.api;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.login.TingShuLogin;
import com.atguigu.tingshu.common.rabbit.constant.MqConst;
import com.atguigu.tingshu.common.rabbit.service.RabbitService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.user.service.UserInfoService;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Tag(name = "微信授权登录接口")
@RestController
@RequestMapping("/api/user/wxLogin")
@Slf4j
public class WxLoginApiController {
    
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private WxMaService wxMaService;
    @Autowired
    private RabbitService rabbitService;
    
    // 获取当前用户登录信息
    //Request URL: http://localhost/api/user/wxLogin/getUserInfo
    //Request Method: GET
    
    /**
     * 根据用户Id获取到用户数据
     *
     * @return
     */
    @TingShuLogin
    @Operation(summary = "获取登录信息")
    @GetMapping("getUserInfo")
    public Result getUserInfo() {
        //  获取到用户Id
        Long userId = AuthContextHolder.getUserId();
        //  调用服务层方法
        UserInfo userInfo = userInfoService.getById(userId);
        // 创建UserInfoVo对象
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo, userInfoVo);
        //  返回数据
        return Result.ok(userInfoVo);
    }
    
    
    // 更新用户信息
    
    /**
     * 更新用户信息
     *
     * @param userInfoVo
     * @return
     */
    @TingShuLogin
    @Operation(summary = "更新用户信息")
    @PostMapping("updateUser")
    public Result updateUser(@RequestBody UserInfoVo userInfoVo) {
        //  获取到用户Id
        Long userId = AuthContextHolder.getUserId();
        UserInfo userInfo = new UserInfo();
        userInfo.setId(userId);
        userInfo.setNickname(userInfoVo.getNickname());
        userInfo.setAvatarUrl(userInfoVo.getAvatarUrl());
        
        //  执行更新方法
        userInfoService.updateById(userInfo);
        return Result.ok();
    }
    
    
    //Request URL: http://localhost/api/user/wxLogin/wxLogin/0a3tonGa1FbbnM0Bv3Ja1je1Fc0tonGB
    //Request Method: GET
    @Operation(summary = "小程序授权登录")
    @GetMapping("/wxLogin/{code}")
    public Result wxLogin(@PathVariable String code) throws WxErrorException {
        // 1. 拿着code + 微信公众平台id + 密钥 请求微信服务器接口，返回openid
        WxMaJscode2SessionResult sessionInfo = wxMaService.getUserService().getSessionInfo(code);
        String openid = sessionInfo.getOpenid();
        // 2. 根据openid判断是否第一次登录
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getWxOpenId, openid);
        UserInfo userInfo = userInfoService.getOne(queryWrapper);
        // 如果第一次登录，添加用户信息，发送mq消息初始化账户
        if (userInfo == null) {
            // 添加用户信息
            //  创建对象
            userInfo = new UserInfo();
            //  赋值用户昵称
            userInfo.setNickname("听友" + System.currentTimeMillis());
            //  赋值用户头像图片
            userInfo.setAvatarUrl("https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
            //  赋值wxOpenId
            userInfo.setWxOpenId(openid);
            //  保存用户信息
            userInfoService.save(userInfo);
            //  发送mq消息初始化账户信息
            rabbitService.sendMessage(MqConst.EXCHANGE_USER, MqConst.ROUTING_USER_REGISTER, userInfo.getId());
            
        }
        
        
        // 3. 生成token，把数据放到redis中
        // redis 的key是token，value是用户信息，设置redis过期时间
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        //redisTemplate.opsForValue().set(token, userInfo, RedisConstant.TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(RedisConstant.USER_LOGIN_KEY_PREFIX + token, userInfo, RedisConstant.USER_LOGIN_KEY_TIMEOUT, TimeUnit.SECONDS);
        
        //4. 返回token
        //  将这个数据存储到map中并返回
        HashMap<String, Object> map = new HashMap<>();
        map.put("token", token);
        //  返回数据
        return Result.ok(map);
    }
    
}
