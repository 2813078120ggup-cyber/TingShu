package com.atguigu.tingshu.common.login;

import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.model.user.UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @className: TingShuLoginAspect
 * @author: gc
 * @date: 2026/9/3 15:00
 * @version: 1.0
 * @description:
 */
@Aspect
@Component
public class TingShuLoginAspect {
    @Autowired
    private RedisTemplate redisTemplate;
    
    // ProceedingJoinPoint:获取被增强方法信息和被增强方法执行
    // 使用环绕通知@Around(切入点表达式)
    @Around("execution(* com.atguigu.tingshu.*.api.*.*(..)) && @annotation(tingShuLogin)")
    
    public Object login(ProceedingJoinPoint joinPoint, TingShuLogin tingShuLogin) throws Throwable {
        // RequestContextHolder 上下文对象 获取request
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = sra.getRequest();
        // 1. 从请求头获取token（前端传递）
        String token = request.getHeader("token");
        
        
        // 2. 根据token查询redis（redis的key是token），如果可以查询到是登录，查询不到则未登录
        // required==true 必须登录  required==false 可以不登录
        boolean isRequired = tingShuLogin.required();
        if (isRequired) {
            // 必须登录
            // 判断请求头中token是否为空，如果为空返回信息
            if (!StringUtils.hasText(token)) {
                throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
            }
            // 如果token不为空，根据token查询redis，判断查询数据是否为空，如果为空，返回登录提示
            UserInfo userInfo = (UserInfo) redisTemplate.opsForValue().get(RedisConstant.USER_LOGIN_KEY_PREFIX+token);
            // 如果为空，返回登录提示信息
            if (userInfo == null) {
                throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
            }
            
            // 登录成功后，执行方法
            return joinPoint.proceed();
            
        } else {
            // 直接执行方法
            return joinPoint.proceed();
        }
    }
}