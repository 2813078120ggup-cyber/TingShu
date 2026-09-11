package com.atguigu.tingshu.account.service.impl;

import com.atguigu.tingshu.account.mapper.RechargeInfoMapper;
import com.atguigu.tingshu.account.service.RechargeInfoService;
import com.atguigu.tingshu.account.service.UserAccountService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.model.account.RechargeInfo;
import com.atguigu.tingshu.vo.account.RechargeInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@SuppressWarnings({"all"})
public class RechargeInfoServiceImpl extends ServiceImpl<RechargeInfoMapper, RechargeInfo> implements RechargeInfoService {
    
    @Autowired
    private RechargeInfoMapper rechargeInfoMapper;
    @Autowired
    private UserAccountService userAccountService;
    
    @Override
    public String submitRecharge(RechargeInfoVo rechargeInfoVo, Long userId) {
        // 创建对象
        RechargeInfo rechargeInfo = new RechargeInfo();
        // 赋值操作
        rechargeInfo.setUserId(userId);
        // 0901-未支付
        rechargeInfo.setRechargeStatus(SystemConstant.ORDER_STATUS_UNPAID);
        //充值金额
        rechargeInfo.setRechargeAmount(rechargeInfoVo.getAmount());
        //充值方式 微信
        rechargeInfo.setPayWay(rechargeInfoVo.getPayWay());
        //充值订单编号
        rechargeInfo.setOrderNo(UUID.randomUUID().toString().replaceAll("-", ""));
        // 插入数据库
        rechargeInfoMapper.insert(rechargeInfo);
        // 返回订单编号
        return rechargeInfo.getOrderNo();
    }
    
    
    @Override
    public RechargeInfo getRechargeInfoByOrderNo(String orderNo) {
        // 根据订单号查询对象
        return this.getOne(new LambdaQueryWrapper<RechargeInfo>().eq(RechargeInfo::getOrderNo, orderNo));
    }
    
    
    /**
     * @param orderNo
     */
    @Override
    @Transactional
    public void rechargePaySuccess(String orderNo) {
        // 获取到充值信息对象
        RechargeInfo rechargeInfo = this.getRechargeInfoByOrderNo(orderNo);
        // 如果当前状态是已支付，则直接返回
        if (SystemConstant.ORDER_STATUS_PAID.equals(rechargeInfo.getRechargeStatus())) return;
        // 否则赋值为已支付状态
        rechargeInfo.setRechargeStatus(SystemConstant.ORDER_STATUS_PAID);
        // 更新数据
        this.updateById(rechargeInfo);
        // 更新余额
        userAccountService.add(rechargeInfo.getUserId(),
                rechargeInfo.getRechargeAmount(),
                rechargeInfo.getOrderNo(),
                SystemConstant.ACCOUNT_TRADE_TYPE_DEPOSIT, "充值");
    }
}