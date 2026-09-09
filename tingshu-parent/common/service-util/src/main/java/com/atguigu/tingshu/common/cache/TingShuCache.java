package com.atguigu.tingshu.common.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TingShuCache {
    /**
     * 缓存key的前缀
     *
     * @return
     */
    String prefix() default "cache";
}
