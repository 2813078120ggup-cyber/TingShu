package com.atguigu.tingshu.user.strategy;

import com.atguigu.tingshu.common.execption.GuiguException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;


//策略工厂：封装可能存在的变化（具体策略实现类）


@Slf4j
@Component
public class StrategyFactory {
    
    
    /**
     * 将IOC容器中ItemTypeStrategy接口下所有实现类对象注入到Map中
     * Map中Key：Bean对象ID
     * Map中Value：实现类对象
     * {"1001":购买类型为专辑策略类对象}
     * {"1002":购买类型为声音策略类对象}
     * {"1003":购买类型为vip策略类对象}
     */
    @Autowired
    private Map<String, ItemTypeStrategy> strategyMap;
    
    /**
     * 根据购买项目类型获取具体策略实现类对象
     *
     * @param itemType 项目类型
     * @return
     */
    public ItemTypeStrategy getStrategy(String itemType) {
        if (strategyMap.containsKey(itemType)) {
            return strategyMap.get(itemType);
        }
        log.error("该策略实现类不存在");
        throw new GuiguException(500, "该策略" + itemType + "实现类不存在");
    }
}