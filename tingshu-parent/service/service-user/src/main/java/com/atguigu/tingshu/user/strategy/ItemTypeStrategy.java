package com.atguigu.tingshu.user.strategy;

import com.atguigu.tingshu.vo.user.UserPaidRecordVo;

/**
 * 抽象：策略接口
 * 定义抽象方法：
 */
public interface ItemTypeStrategy {

    /**
     * 处理用户购买记录
     *
     * @param userPaidRecordVo
     */
    public void savePaidRecord(UserPaidRecordVo userPaidRecordVo);
}