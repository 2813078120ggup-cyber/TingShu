package com.atguigu.tingshu.album.controller;

import com.atguigu.tingshu.album.service.TestService;
import com.atguigu.tingshu.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author atguigu-mqx
 * @ClassName TestController
 * @description: TODO
 * @date 2023年08月22日
 * @version: 1.0
 */
@RestController
@RequestMapping("api/album/test")
public class TestController {

    //  注入服务层方法
    @Autowired
    private TestService testService;

    /**
     * 测试分布式锁
     * @return
     */
    @GetMapping("testLock")
    public Result testLock(){
        testService.testLock();
        return Result.ok();
    }
}