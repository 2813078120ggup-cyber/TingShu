package com.atguigu.tingshu.user.client;

import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.user.VipServiceConfig;
import com.atguigu.tingshu.user.client.impl.VipServiceConfigDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * <p>
 * 产品列表API接口
 * </p>
 *
 *
 */
@FeignClient(value = "service-user", fallback = VipServiceConfigDegradeFeignClient.class)
public interface VipServiceConfigFeignClient {
    
    
    /**
     * 判断用户是否购买过专辑
     *
     * @param albumId
     * @return
     */
    @GetMapping("api/user/userInfo/isPaidAlbum/{albumId}")
    Result<Boolean> isPaidAlbum(@PathVariable("albumId") Long albumId);
    
    
    /**
     * 根据itemId获取VIP服务配置信息
     *
     * @param id
     * @return
     */
    @GetMapping("api/user/vipServiceConfig/getVipServiceConfig/{id}")
    Result<VipServiceConfig> getVipServiceConfig(@PathVariable("id") Long id);
}