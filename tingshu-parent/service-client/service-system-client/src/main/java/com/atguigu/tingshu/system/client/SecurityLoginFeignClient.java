package com.atguigu.tingshu.system.client;

import com.atguigu.tingshu.system.client.impl.SecurityLoginDegradeFeignClient;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.system.SysUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * <p>
 * 产品列表API接口
 * </p>
 *
 * 
 */
@FeignClient(value = "service-system", fallback = SecurityLoginDegradeFeignClient.class)
public interface SecurityLoginFeignClient {

    /**
     * 根据用户名获取用户信息
     * @param username
     * @return
     */
    @GetMapping("/admin/system/securityLogin/getByUsername/{username}")
    Result<SysUser> getByUsername(@PathVariable("username") String username);

    /**
     * 获取用户按钮权限
     * @param userId
     * @return
     */
    @GetMapping("/admin/system/securityLogin/findUserPermsList/{userId}")
    Result<List<String>> findUserPermsList(@PathVariable("userId") Long userId);
}