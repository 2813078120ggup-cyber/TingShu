package com.atguigu.tingshu.user.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaCloudServiceImpl;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.WxMaConfig;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author atguigu-mqx
 * @ClassName WeChatMpConfig
 * @description: TODO
 * @date 2023年05月20日
 * @version: 1.0
 */
@Component
public class WeChatMpConfig {

    @Autowired
    private WechatAccountConfig wechatAccountConfig;

    @Bean
    public WxMaService wxMaService(){
        //  创建对象
        WxMaDefaultConfigImpl wxMaConfig =  new WxMaDefaultConfigImpl();
        wxMaConfig.setAppid(wechatAccountConfig.getAppId());
        wxMaConfig.setSecret(wechatAccountConfig.getAppSecret());
        wxMaConfig.setMsgDataFormat("JSON");
        //  创建 WxMaService 对象
        WxMaService service = new WxMaServiceImpl();
        //  给 WxMaService 设置配置选项
        service.setWxMaConfig(wxMaConfig);
        return service;
    }
}