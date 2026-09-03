package com.atguigu.tingshu.common.login;


import java.lang.annotation.*;

// 元注解：定义注解特性的注解
@Documented
@Target(ElementType.METHOD)  // 设置注解可以使用在什么地方，比如类、方法、属性上
@Retention(RetentionPolicy.RUNTIME)  // 在什么时候生效（一半runtime）
public @interface TingShuLogin {
    //@TingshuLogin(value = "123")
    // 类型  属性名称()  default 默认值
    //String value() default "";
    
    /**
     * 是否必须要登录
     *
     * @return
     */
    boolean required() default true;
}
