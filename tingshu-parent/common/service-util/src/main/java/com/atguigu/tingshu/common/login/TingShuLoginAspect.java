package com.atguigu.tingshu.common.login;

import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.common.util.AuthContextHolder;
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
        // 请求线程会被复用，先清理可能残留的用户身份。
        AuthContextHolder.removeUserId();
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            ServletRequestAttributes sra = (ServletRequestAttributes) requestAttributes;
            HttpServletRequest request = sra.getRequest();
            String token = request.getHeader("token");

            // 可选登录也需要识别有效 token，required 只决定是否允许游客访问。
            UserInfo userInfo = null;
            if (StringUtils.hasText(token)) {
                userInfo = (UserInfo) redisTemplate.opsForValue()
                        .get(RedisConstant.USER_LOGIN_KEY_PREFIX + token);
            }
            if (userInfo != null && userInfo.getId() != null) {
                AuthContextHolder.setUserId(userInfo.getId());
            } else if (tingShuLogin.required()) {
                throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
            }

            return joinPoint.proceed();
        } finally {
            // 正常返回、鉴权失败或业务异常时都清理当前线程的用户身份。
            AuthContextHolder.removeUserId();
        }
    }
}
