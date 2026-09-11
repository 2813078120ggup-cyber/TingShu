package com.atguigu.tingshu.account.api;

import com.atguigu.tingshu.account.service.UserAccountService;
import com.atguigu.tingshu.common.login.TingShuLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.account.UserAccountDetail;
import com.atguigu.tingshu.vo.account.AccountLockVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "用户账户管理")
@RestController
@RequestMapping("api/account/userAccount")
@SuppressWarnings({"all"})
public class UserAccountApiController {
    
    @Autowired
    private UserAccountService userAccountService;
    
    /**
     * 获取账户可用余额
     *
     * @return
     */
    @TingShuLogin
    @Operation(summary = "获取账号可用金额")
    @GetMapping("getAvailableAmount")
    public Result<BigDecimal> getAvailableAmount() {
        //	调用服务层方法
        return Result.ok(userAccountService.getAvailableAmount(AuthContextHolder.getUserId()));
    }
    
    
    /**
     * 检查及扣减账户余额
     *
     * @param accountDeductVo
     * @return
     */
    @Operation(summary = "检查及扣减账户余额")
    @PostMapping("/checkAndDeduct")
    public Result checkAndDeduct(@RequestBody AccountLockVo accountLockVo) {
        // 调用服务此方法.
        userAccountService.checkAndDeduct(accountLockVo);
        // 返回数据
        return Result.ok();
    }
    
    
    /**
     * 用户充值记录
     *
     * @param page
     * @param limit
     * @return
     */
    @TingShuLogin
    @Operation(summary = "获取用户充值记录")
    @GetMapping("/findUserRechargePage/{page}/{limit}")
    public Result findUserRechargePage(
            @Parameter(name = "page", description = "当前页码", required = true)
            @PathVariable Long page,
            
            @Parameter(name = "limit", description = "每页记录数", required = true)
            @PathVariable Long limit) {
        // 获取到用户Id
        Long userId = AuthContextHolder.getUserId();
        // 构建Page对象
        Page<UserAccountDetail> pageParam = new Page<>(page, limit);
        // 调用服务层方法
        IPage<UserAccountDetail> pageModel = userAccountService.findUserRechargePage(pageParam, userId);
        // 返回数据
        return Result.ok(pageModel);
    }
    
    @TingShuLogin
    @Operation(summary = "获取用户消费记录")
    @GetMapping("findUserConsumePage/{page}/{limit}")
    public Result findUserConsumePage(
            @Parameter(name = "page", description = "当前页码", required = true)
            @PathVariable Long page,
            
            @Parameter(name = "limit", description = "每页记录数", required = true)
            @PathVariable Long limit) {
        // 获取到用户Id
        Long userId = AuthContextHolder.getUserId();
        Page<UserAccountDetail> pageParam = new Page<>(page, limit);
        IPage<UserAccountDetail> pageModel = userAccountService.findUserConsumePage(pageParam, userId);
        return Result.ok(pageModel);
    }
}
