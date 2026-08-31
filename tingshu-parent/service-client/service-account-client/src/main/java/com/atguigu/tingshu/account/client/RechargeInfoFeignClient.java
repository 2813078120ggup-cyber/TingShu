package com.atguigu.tingshu.account.client;

import com.atguigu.tingshu.account.client.impl.RechargeInfoDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * <p>
 * 产品列表API接口
 * </p>
 *
 */
@FeignClient(value = "service-account", fallback = RechargeInfoDegradeFeignClient.class)
public interface RechargeInfoFeignClient {


}