package com.atguigu.tingshu.account.api;

import com.atguigu.tingshu.account.service.RechargeInfoService;
import com.atguigu.tingshu.common.login.TingShuLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.account.RechargeInfo;
import com.atguigu.tingshu.vo.account.RechargeInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@Tag(name = "充值管理")
@RestController
@RequestMapping("api/account/rechargeInfo")
@SuppressWarnings({"all"})
public class RechargeInfoApiController {
    
    @Autowired
    private RechargeInfoService rechargeInfoService;
    
    /**
     * 给用户充值
     *
     * @param rechargeInfoVo
     * @return
     */
    @TingShuLogin
    @Operation(summary = "充值")
    @PostMapping("submitRecharge")
    public Result submitRecharge(@RequestBody RechargeInfoVo rechargeInfoVo) {
        //	获取到用户Id
        Long userId = AuthContextHolder.getUserId();
        //	调用充值方法
        String orderNo = this.rechargeInfoService.submitRecharge(rechargeInfoVo, userId);
        //	创建map 集合对象
        HashMap<String, Object> map = new HashMap<>();
        //	存储订单Id
        map.put("orderNo", orderNo);
        //	返回数据
        return Result.ok(map);
    }
    
    
    /**
     * 根据订单号获取充值信息
     *
     * @param orderNo
     * @return
     */
    @TingShuLogin
    @Operation(summary = "根据订单号获取充值信息")
    @GetMapping("/getRechargeInfo/{orderNo}")
    public Result<RechargeInfo> getRechargeInfo(@PathVariable("orderNo") String orderNo) {
        // 调用服务层方法
        RechargeInfo rechargeInfo = rechargeInfoService.getRechargeInfoByOrderNo(orderNo);
        // 返回对象
        return Result.ok(rechargeInfo);
    }
}

