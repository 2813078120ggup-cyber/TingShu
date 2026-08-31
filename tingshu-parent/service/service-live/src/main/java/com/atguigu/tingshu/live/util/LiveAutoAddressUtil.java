package com.atguigu.tingshu.live.util;

import com.atguigu.tingshu.vo.live.TencentLiveAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 推拉流生成工具
 */
public class LiveAutoAddressUtil {

    private static char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    //  推流域名
    private static String pushDomain = "190649.push.tlivecloud.com"; //云直播控制台配置的推流域名
    //  拉流域名
    private static String pullDomain = "mqxplay.atguigu.cn";//云直播控制台配置的拉流域名
    private static String AppName = "live"; //直播SDK-->应用管理-->自己创建应用中的应用名称


    /**
     * 获取到推流地址，拉流地址给 TencentLiveAddressVo 对象
     * @param streamName
     * @param txTime
     * @return
     */
    public static TencentLiveAddressVo getAddressUrl(String streamName, long txTime) {
        String pushKey = System.getProperty("tingshu.live.push-key", System.getenv("LIVE_PUSH_KEY"));
        if (pushKey == null || pushKey.isBlank()) {
            throw new IllegalStateException("Configure LIVE_PUSH_KEY or tingshu.live.push-key before generating URLs");
        }
        //  获取到鉴权key
        String safeUrl = getSafeUrl(pushKey, streamName, txTime);
        //
        TencentLiveAddressVo liveAddress = new TencentLiveAddressVo();
        //   String pushUrl= "webrtc://190649.push.tlivecloud.com/live/"+streamName+"?"+safeUrl;
        liveAddress.setPushWebRtcUrl("webrtc://" + pushDomain + "/" + AppName + "/" + streamName + "?" + safeUrl);
        liveAddress.setPullWebRtcUrl("webrtc://" + pullDomain + "/" + AppName + "/" + streamName + "?" + safeUrl);
        return liveAddress;
    } /* * KEY+ streamName + txTime */

    private static String getSafeUrl(String key, String streamName, long txTime) {
        String input = new StringBuilder().append(key).append(streamName).append(Long.toHexString(txTime).toUpperCase()).toString();
        String txSecret = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            txSecret = byteArrayToHexString(messageDigest.digest(input.getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return txSecret == null ? "" : new StringBuilder().append("txSecret=").append(txSecret).append("&").append("txTime=").append(Long.toHexString(txTime).toUpperCase()).toString();
    }

    private static String byteArrayToHexString(byte[] data) {
        char[] out = new char[data.length << 1];
        for (int i = 0, j = 0; i < data.length; i++) {
            out[j++] = DIGITS_LOWER[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_LOWER[0x0F & data[i]];
        }
        return new String(out);
    }

    public static void main(String[] args) {
        String streamName = "test";
        LocalDateTime localDateTime = LocalDateTime.now();
        long nowTime = localDateTime.toEpochSecond(ZoneOffset.of("+8"));
        long endTime = nowTime + 60 * 60 * 12; // 默认12小时
        TencentLiveAddressVo addressUrl = LiveAutoAddressUtil.getAddressUrl(streamName, endTime);
        System.out.println(addressUrl.getPushWebRtcUrl());
        System.out.println(addressUrl.getPullWebRtcUrl());
    }
}