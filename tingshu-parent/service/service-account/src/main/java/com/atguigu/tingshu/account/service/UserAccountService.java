package com.atguigu.tingshu.account.service;

import com.atguigu.tingshu.model.account.UserAccount;
import com.atguigu.tingshu.model.account.UserAccountDetail;
import com.atguigu.tingshu.vo.account.AccountLockVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

public interface UserAccountService extends IService<UserAccount> {
    
    // 添加用户账户
    void addUserAccount(Long userId);
    
    // 获取账户可用余额
    
    /**
     * 获取账户可用余额
     *
     * @param userId
     * @return
     */
    BigDecimal getAvailableAmount(Long userId);
    
    /**
     * 根据用户Id 获取到可用余额对象
     *
     * @param userId
     * @return
     */
    UserAccount getUserAccountByUserId(Long userId);
    
    // 检查及扣减账户余额
    int checkAndDeduct(AccountLockVo accountLockVo);
    
    /**
     * 充值
     *
     * @param userId
     * @param amount
     * @param orderNo
     * @param tradeType
     * @param title
     */
    void add(Long userId, BigDecimal amount, String orderNo, String tradeType, String title);
    
    /**
     * 查看用户充值记录
     *
     * @param pageParam
     * @param userId
     * @return
     */
    IPage<UserAccountDetail> findUserRechargePage(Page<UserAccountDetail> pageParam, Long userId);
    
    /**
     * 消费记录
     *
     * @param pageParam
     * @param userId
     * @return
     */
    IPage<UserAccountDetail> findUserConsumePage(Page<UserAccountDetail> pageParam, Long userId);
}
