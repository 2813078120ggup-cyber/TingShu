package com.atguigu.tingshu.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.tingshu.album.client.AlbumInfoFeignClient;
import com.atguigu.tingshu.album.client.TrackInfoFeignClient;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.model.order.OrderInfo;
import com.atguigu.tingshu.model.user.VipServiceConfig;
import com.atguigu.tingshu.order.helper.SignHelper;
import com.atguigu.tingshu.order.mapper.OrderInfoMapper;
import com.atguigu.tingshu.order.service.OrderInfoService;
import com.atguigu.tingshu.user.client.UserInfoFeignClient;
import com.atguigu.tingshu.user.client.VipServiceConfigFeignClient;
import com.atguigu.tingshu.vo.order.OrderDerateVo;
import com.atguigu.tingshu.vo.order.OrderDetailVo;
import com.atguigu.tingshu.vo.order.OrderInfoVo;
import com.atguigu.tingshu.vo.order.TradeVo;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {
    
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private UserInfoFeignClient userInfoFeignClient;
    @Autowired
    private AlbumInfoFeignClient albumInfoFeignClient;
    @Autowired
    private VipServiceConfigFeignClient vipServiceConfigFeignClient;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TrackInfoFeignClient trackInfoFeignClient;
    
    
    // 确认订单
    @Override
    public OrderInfoVo trade(TradeVo tradeVo, Long userId) {
        // 1. 远程调用：根据userId获取用户信息
        Result<UserInfoVo> userInfoVoResult = userInfoFeignClient.getUserInfoVo(userId);
        Assert.notNull(userInfoVoResult);
        UserInfoVo userInfoVo = userInfoVoResult.getData();
        Assert.notNull(userInfoVo);
        //  订单原始金额
        BigDecimal originalAmount = new BigDecimal("0.00");
        //  减免总金额
        BigDecimal derateAmount = new BigDecimal("0.00");
        //  订单总价
        BigDecimal orderAmount = new BigDecimal("0.00");
        //  订单明细集合
        List<OrderDetailVo> orderDetailVoList = new ArrayList<>();
        //  订单减免明细列表
        List<OrderDerateVo> orderDerateVoList = new ArrayList<>();
        
        // 2. 判断购买类型
        // 3. 判断为 1001 专辑
        if ((SystemConstant.ORDER_ITEM_TYPE_ALBUM).equals(tradeVo.getItemType())) {
            // 3.1 判断用户是否购买过专辑，如果用户购买过，不需要重新购买
            Result<Boolean> isPaidAlbumResult = userInfoFeignClient.isPaidAlbum(tradeVo.getItemId());
            Assert.notNull(isPaidAlbumResult);
            // true:购买过
            Boolean isPaidAlbum = isPaidAlbumResult.getData();
            Assert.notNull(isPaidAlbum);
            if (isPaidAlbum) {
                throw new GuiguException(ResultCodeEnum.REPEAT_BUY_ERROR);
            }
            
            // 3.2 根据专辑Id 获取到专辑数据
            Result<AlbumInfo> albumInfoResult = albumInfoFeignClient.getAlbumInfo(tradeVo.getItemId());
            Assert.notNull(albumInfoResult, "返回专辑结果集不能为空");
            AlbumInfo albumInfo = albumInfoResult.getData();
            Assert.notNull(albumInfo, "专辑对象不能为空");
            // 3.3 判断当前用户是否是vip，获取原始金额，获取折扣，计算优惠金额和最终金额
            // 0:不是vip或vip过期
            if (userInfoVo.getIsVip().intValue() == 0 ||
                    (userInfoVo.getIsVip().intValue() == 1 && userInfoVo.getVipExpireTime().before(new Date()))) {
                // 获取专辑价格
                originalAmount = albumInfo.getPrice();
                // 判断是否打折 , 不等于-1 就是打折
                if (albumInfo.getDiscount().intValue() != -1) {
                    // 打折 100 8  100*0.8
                    // 优惠金额 = 原价 × (10 - 折扣) ÷ 10
                    derateAmount = originalAmount.multiply(new BigDecimal("10")
                                    .subtract(albumInfo.getDiscount()))
                            .divide(new BigDecimal(10), 2, RoundingMode.HALF_UP);
                }
                //  订单总价
                orderAmount = originalAmount.subtract(derateAmount);
            } else {
                // 是VIP会员
                originalAmount = albumInfo.getPrice();
                //discount=-1,不打折，折扣如：8折 9.5折
                if (albumInfo.getVipDiscount().intValue() != -1) {
                    derateAmount = albumInfo.getPrice().multiply(new BigDecimal(10).subtract(albumInfo.getVipDiscount())).divide(new BigDecimal(10), 2, RoundingMode.HALF_UP);
                }
                //  订单总价
                orderAmount = originalAmount.subtract(derateAmount);
            }
            // 3.4 封装其他数据
            //  订单明细
            OrderDetailVo orderDetailVo = new OrderDetailVo();
            orderDetailVo.setItemId(tradeVo.getItemId());
            orderDetailVo.setItemName(albumInfo.getAlbumTitle());
            orderDetailVo.setItemUrl(albumInfo.getCoverUrl());
            orderDetailVo.setItemPrice(albumInfo.getPrice());
            orderDetailVoList.add(orderDetailVo);
            
            // 订单减免列表
            // 原始金额-最终金额
            if (originalAmount.subtract(orderAmount).doubleValue() != 0) {
                OrderDerateVo orderDerateVo = new OrderDerateVo();
                orderDerateVo.setDerateType(SystemConstant.ORDER_DERATE_ALBUM_DISCOUNT);
                orderDerateVo.setDerateAmount(originalAmount.subtract(orderAmount));
                orderDerateVoList.add(orderDerateVo);
            }
        }
        // 4. 判断为 1003 vip
        else if ((SystemConstant.ORDER_ITEM_TYPE_VIP).equals(tradeVo.getItemType())) {
            //  根据id 获取VIP 服务配置信息
            Result<VipServiceConfig> vipServiceConfigResult = vipServiceConfigFeignClient.getVipServiceConfig(tradeVo.getItemId());
            Assert.notNull(vipServiceConfigResult, "返回vip配置结果集不能为空");
            VipServiceConfig vipServiceConfig = vipServiceConfigResult.getData();
            Assert.notNull(vipServiceConfig, "返回vip配置对象不能为空");
            
            // 原始金额
            originalAmount = vipServiceConfig.getPrice();
            derateAmount = vipServiceConfig.getPrice().subtract(vipServiceConfig.getDiscountPrice());
            orderAmount = originalAmount.subtract(derateAmount);
            
            //订单明细
            OrderDetailVo orderDetailVo = new OrderDetailVo();
            orderDetailVo.setItemId(tradeVo.getItemId());
            orderDetailVo.setItemName("VIP会员" + vipServiceConfig.getName());
            orderDetailVo.setItemUrl(vipServiceConfig.getImageUrl());
            orderDetailVo.setItemPrice(vipServiceConfig.getDiscountPrice());
            orderDetailVoList.add(orderDetailVo);
            
            //添加订单减免
            if (originalAmount.subtract(orderAmount).doubleValue() != 0) {
                OrderDerateVo orderDerateVo = new OrderDerateVo();
                orderDerateVo.setDerateType(SystemConstant.ORDER_DERATE_VIP_SERVICE_DISCOUNT);
                orderDerateVo.setDerateAmount(originalAmount.subtract(orderAmount));
                orderDerateVoList.add(orderDerateVo);
            }
        }
        // 5.  判断为 1002 声音
        else if (tradeVo.getItemType().equals(SystemConstant.ORDER_ITEM_TYPE_TRACK)) {
            //  判断tradeVo.getTrackCount():购买集数（本集/后n集）
            if (tradeVo.getTrackCount().intValue() < 0) {
                throw new GuiguException(ResultCodeEnum.ARGUMENT_VALID_ERROR);
            }
            //  获取下单声音列表
            Result<List<TrackInfo>> trackInfoListResult =
                    trackInfoFeignClient.findPaidTrackInfoList(tradeVo.getItemId(), tradeVo.getTrackCount());
            System.out.println("trackInfoListResult = " + trackInfoListResult);
            
            Assert.notNull(trackInfoListResult,
                    "查询待支付声音Feign结果不能为空");
            List<TrackInfo> trackInfoList = trackInfoListResult.getData();
            System.out.println("trackInfoList = " + trackInfoList);
            Assert.notEmpty(trackInfoList,
                    "没有查询到需要购买的声音");
            //  设定：购买声音不支持折扣
            // 获取下单列表中第一个声音id，再根据声音id获取专辑id，再根据专辑id获取专辑信息
            Result<AlbumInfo> albumInfoResult = albumInfoFeignClient.getAlbumInfo(trackInfoList.get(0).getAlbumId());
            AlbumInfo albumInfo = albumInfoResult.getData();
            // >0 :后n集    =0:本集
            originalAmount = tradeVo.getTrackCount().intValue() > 0
                    ? albumInfo.getPrice().multiply(new BigDecimal(tradeVo.getTrackCount()))
                    : albumInfo.getPrice();
            //  计算订单总价
            orderAmount = originalAmount;
            
            //  循环遍历声音集合对象赋值订单明细
            orderDetailVoList = trackInfoList.stream().map(trackInfo -> {
                OrderDetailVo orderDetailVo = new OrderDetailVo();
                orderDetailVo.setItemId(trackInfo.getId());
                orderDetailVo.setItemUrl(trackInfo.getCoverUrl());
                orderDetailVo.setItemPrice(albumInfo.getPrice());
                orderDetailVo.setItemName(trackInfo.getTrackTitle());
                return orderDetailVo;
            }).collect(Collectors.toList());
        }
        
        
        // 7. 防止重复提交（防重）：生成一个唯一标识，保存到redis中一份
        String tradeNoKey = "user:trade:" + userId;
        // 定义一个流水号
        String tradeNo = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(tradeNoKey, tradeNo);
        
        // 6. 将最终数据封装到OrderInfoVo，返回OrderInfoVo对象
        //构造结果
        OrderInfoVo orderInfoVo = new OrderInfoVo();
        orderInfoVo.setItemType(tradeVo.getItemType());
        orderInfoVo.setOriginalAmount(originalAmount);
        orderInfoVo.setDerateAmount(derateAmount);
        orderInfoVo.setOrderAmount(orderAmount);
        orderInfoVo.setTradeNo(tradeNo);
        orderInfoVo.setOrderDetailVoList(orderDetailVoList);
        orderInfoVo.setOrderDerateVoList(orderDerateVoList);
        orderInfoVo.setTimestamp(SignHelper.getTimestamp());
        
        // 8. 加密处理
        //  支付方式默认值 目的是防止用户在前端篡改金额数据
        orderInfoVo.setPayWay(SystemConstant.ORDER_PAY_WAY_WEIXIN);
        //  生成签名
        Map<String, Object> parameterMap = JSON.parseObject(JSON.toJSONString(orderInfoVo), Map.class);
        // {"orderDerateVoList":[{"derateType":"1406","derateAmount":20.00}],"itemType":"1003","orderAmount":40.00,"originalAmount":60.00,"tradeNo":"ed2628826ba54ebfa41632f9173a780d","derateAmount":20.00,"payWay":"","orderDetailVoList":[{"itemId":2,"itemName":"购买VIP3个月","itemPrice":40.00,"itemUrl":"http://39.98.123.211/group1/vip.png"}],"timestamp":1699606865851}
        String sign = SignHelper.getSign(parameterMap);
        orderInfoVo.setSign(sign);
        //  返回对象
        return orderInfoVo;
        
        
    }
}
