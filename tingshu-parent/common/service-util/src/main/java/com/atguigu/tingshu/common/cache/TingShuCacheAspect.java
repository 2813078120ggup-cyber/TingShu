package com.atguigu.tingshu.common.cache;

import com.atguigu.tingshu.common.constant.RedisConstant;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class TingShuCacheAspect {
    @Autowired
    private RedisTemplate redisTemplate;
    
    @Autowired
    private RedissonClient redissonClient;
    
    @SneakyThrows
    @Around("@annotation(com.atguigu.tingshu.common.cache.TingShuCache)")
    public Object cacheAspect(ProceedingJoinPoint joinPoint) {
        Object obj = null;
        // 1. 从@TingShuCache注解方法中获取prefix值  @TingShuCache(prefix = RedisConstant.ALBUM_INFO_PREFIX)
        // 从@TingShuCache注解方法中获取参数值  public Result<AlbumInfo> getAlbumInfo(@PathVariable Long albumId)
        // 获取参数列表   getArgs:获取当前被 AOP 拦截方法的所有参数。
        Object[] args = joinPoint.getArgs();
        // 获取注解的prefix值
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        TingShuCache tingShuCache = methodSignature.getMethod().getAnnotation(TingShuCache.class);
        // 获取主键前缀
        String prefix = tingShuCache.prefix();
        // 组成缓存的key
        String key = prefix + Arrays.asList(args);
        try {
            // 2. 查询redis
            obj = this.redisTemplate.opsForValue().get(key);
            // 如果redis中没有数据
            if (obj == null) {
                // 声明分布式锁key
                RLock lock = redissonClient.getLock(key + ":lock");
                boolean result = lock.tryLock(
                        RedisConstant.CACHE_LOCK_EXPIRE_PX1,
                        RedisConstant.CACHE_LOCK_EXPIRE_PX2,
                        TimeUnit.SECONDS);
                if (result) {// 获取锁成功
                    try { // 查询mysql
                        // 执行被注解的方法
                        obj = joinPoint.proceed(args);
                        if (null == obj) { // 数据库中没有数据
                            // 把结果存入redis
                            Object o = new Object();
                            // 解决缓存穿透：空结果也进行缓存
                            // 数据库虽然不存在，但 Redis 先记录一下“这个 key 已经查过了，是不存在的”。
                            this.redisTemplate.opsForValue().set(key, o, RedisConstant.CACHE_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                            return o;
                        }
                        // 数据库中有数据
                        // 把结果存入redis
                        this.redisTemplate.opsForValue().set(key, obj, RedisConstant.CACHE_TIMEOUT, TimeUnit.SECONDS);
                        return obj;
                    } finally {
                        // 释放资源
                        lock.unlock();
                    }
                } else {
                    //  没有获取到锁的用户自旋
                    return cacheAspect(joinPoint);
                }
            } else {
                return obj;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        // 降级：如果缓存实现了异常，暂时从mysql数据库获取数据
        return joinPoint.proceed(args);
    }
}