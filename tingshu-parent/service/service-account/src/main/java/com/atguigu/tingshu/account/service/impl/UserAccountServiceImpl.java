package com.atguigu.tingshu.account.service.impl;

import com.atguigu.tingshu.account.mapper.UserAccountDetailMapper;
import com.atguigu.tingshu.account.mapper.UserAccountMapper;
import com.atguigu.tingshu.account.service.UserAccountService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.model.account.UserAccount;
import com.atguigu.tingshu.model.account.UserAccountDetail;
import com.atguigu.tingshu.vo.account.AccountLockVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {
    
    @Autowired
    private UserAccountMapper userAccountMapper;
    @Autowired
    private UserAccountDetailMapper userAccountDetailMapper;
    
    @Override
    public void addUserAccount(Long userId) {
        // user_account
        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(userId);
        userAccountMapper.insert(userAccount);
    }
    
    // 获取账户可用余额
    @Override
    public BigDecimal getAvailableAmount(Long userId) {
        //	根据用户Id 获取到用户余额对象
        UserAccount userAccount = this.getUserAccountByUserId(userId);
        return userAccount.getAvailableAmount();
    }
    
    @Override
    public UserAccount getUserAccountByUserId(Long userId) {
        return this.getOne(new LambdaQueryWrapper<UserAccount>().eq(UserAccount::getUserId, userId));
    }
    
    // 检查及扣减账户余额
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int checkAndDeduct(AccountLockVo accountLockVo) {
        /*//  调用mapper 层方法.
        int count = userAccountMapper.checkAndDeduct(accountLockVo.getUserId(), accountLockVo.getAmount());*/
        
        // mybatis-plus实现：
        // 泛型：要查询的实体类
        LambdaQueryWrapper<UserAccount> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAccount::getUserId, accountLockVo.getUserId())
                .ge(UserAccount::getAvailableAmount, accountLockVo.getAmount());
        
        // 根据userId查账户原始金额，减去支付金额，把最终金额设置到userAccount对象中
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAccount::getUserId, accountLockVo.getUserId());
        UserAccount userAccount = userAccountMapper.selectOne(wrapper);
        // 原始金额-支付金额
        userAccount.setAvailableAmount(userAccount.getAvailableAmount().subtract(accountLockVo.getAmount()));
        BigDecimal amount = accountLockVo.getAmount();
        userAccount.setTotalAmount(userAccount.getTotalAmount().subtract(amount));
        int count = userAccountMapper.update(userAccount, queryWrapper);
        
        //  判断
        if (count == 0) {
            throw new GuiguException(400, "账户余额不足！");
        }
        //  记录明细
        this.addUserAccountDetail(
                accountLockVo.getUserId(),
                accountLockVo.getContent(),
                SystemConstant.ACCOUNT_TRADE_TYPE_MINUS,
                accountLockVo.getAmount(),
                accountLockVo.getOrderNo()
        );
        //  返回受影响的行数
        return count;
    }
    
    private void addUserAccountDetail(Long userId, String title, String tradeType, BigDecimal amount, String orderNo) {
        UserAccountDetail userAccountDetail = new UserAccountDetail();
        userAccountDetail.setUserId(userId);
        userAccountDetail.setTitle(title);
        userAccountDetail.setTradeType(tradeType);
        userAccountDetail.setAmount(amount);
        userAccountDetail.setOrderNo(orderNo);
        userAccountDetailMapper.insert(userAccountDetail);
    }
}
