package com.atguigu.tingshu.user.client;

import com.atguigu.tingshu.user.client.impl.UserListenProcessDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * <p>
 * 产品列表API接口
 * </p>
 *
 * 
 */
@FeignClient(value = "service-user", fallback = UserListenProcessDegradeFeignClient.class)
public interface UserListenProcessFeignClient {

}