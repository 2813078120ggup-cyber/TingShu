package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.service.TestService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @className: TestServiceImpl
 * @author: gc
 * @date: 2026/9/8 17:41
 * @version: 1.0
 * @description:
 */
@Service
public class TestServiceImpl implements TestService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    /*@Override
    public synchronized void testLock() {
        try {
            //1.从Redis缓存中获取key="num"的值  保证redis中存在"num"(手动提前在redis中创建key)
            String value = stringRedisTemplate.opsForValue().get("num");
            if (StringUtils.isBlank(value)) {
                return;
            }
            //2.对获取到值进行+1操作
            int num = Integer.parseInt(value);
            stringRedisTemplate.opsForValue().set("num", String.valueOf(++num));
            
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }*/
    
    /**
     * 原理：执行业务方法前先尝试获取锁（setnx存入key val），如果获取锁成功再执行业务代码，业务执行完毕后将锁释放(del key)
     */
    /*@Override
    public void testLock() {
        
        //0.先尝试获取锁 setnx key val
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent("lock", "lock", 10, TimeUnit.SECONDS);
        if (flag) {
            try {
                //获取锁成功，执行业务代码
                //1.先从redis中通过key num获取值  key提前手动设置 num 初始值：0
                String value = stringRedisTemplate.opsForValue().get("num");
                //2.如果值为空则非法直接返回即可
                if (StringUtils.isBlank(value)) {
                    return;
                }
                //3.对num值进行自增加一
                int num = Integer.parseInt(value);
                stringRedisTemplate.opsForValue().set("num", String.valueOf(++num));
                
                
            } finally {
                //4.将锁释放
                stringRedisTemplate.delete("lock");
            }
        } else {
            try {
                Thread.sleep(100);
                this.testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }*/
    
    
    /*@Override
    public synchronized void testLock() {
        //  声明uuid
        String uuid = UUID.randomUUID().toString();
        //  将uuid 存储到缓存中
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS);
        if (flag) {
            try {
                //1.从Redis缓存中获取key="num"的值  保证redis中存在"num"(手动提前在redis中创建key)
                String value = stringRedisTemplate.opsForValue().get("num");
                if (StringUtils.isBlank(value)) {
                    return;
                }
                //2.对获取到值进行+1操作
                int num = Integer.parseInt(value);
                stringRedisTemplate.opsForValue().set("num", String.valueOf(++num));
                //  判断uuid 是否与缓存中的数据相等
                if (uuid.equals(stringRedisTemplate.opsForValue().get("lock"))) {
                    //  如果相等，删除锁
                    stringRedisTemplate.delete("lock");
                }
                
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Thread.sleep(100);
                this.testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
    }*/
    
    /**
     * 采用SpringDataRedis实现分布式锁
     * 原理：执行业务方法前先尝试获取锁（setnx存入key val），如果获取锁成功再执行业务代码，业务执行完毕后将锁释放(del key)
     */
    @Override
    public void testLock() {
        
        //0.先尝试获取锁 setnx key val
        //问题：锁可能存在线程间相互释放
        //Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent("lock", "lock", 10, TimeUnit.SECONDS);
        //解决：锁值设置为uuid
        String uuid = UUID.randomUUID().toString();
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 10, TimeUnit.SECONDS);
        
        if (flag) {
            //获取锁成功，执行业务代码
            //1.先从redis中通过key num获取值  key提前手动设置 num 初始值：0
            String value = stringRedisTemplate.opsForValue().get("num");
            //2.如果值为空则非法直接返回即可
            if (StringUtils.isBlank(value)) {
                return;
            }
            //3.对num值进行自增加一
            int num = Integer.parseInt(value);
            stringRedisTemplate.opsForValue().set("num", String.valueOf(++num));
            
            //4.将锁释放 判断uuid
            //问题：删除操作缺乏原子性。
            //if(uuid.equals(stringRedisTemplate.opsForValue().get("lock"))){ //线程一：判断是满足是当前线程锁的值
            //    //条件满足，此时锁正好到期，redis锁自动释放了线程2获取锁成功，线程1将线程2的锁删除
            //    stringRedisTemplate.delete("lock");
            //}
            //解决：redis执行lua脚本保证原子，lua脚本执行会作为一个整体执行
            
            //执行脚本参数 参数1：脚本对象封装lua脚本，参数二：lua脚本中需要key参数（KEYS[i]）  参数三：lua脚本中需要参数值 ARGV[i]
            //4.1 先创建脚本对象 DefaultRedisScript泛型脚本语言返回值类型 Long 0：失败 1：成功
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            //4.2设置脚本文本
            String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                    "then\n" +
                    "    return redis.call(\"del\",KEYS[1])\n" +
                    "else\n" +
                    "    return 0\n" +
                    "end";
            redisScript.setScriptText(script);
            //4.3 设置响应类型
            redisScript.setResultType(Long.class);
            stringRedisTemplate.execute(redisScript, Arrays.asList("lock"), uuid);
        } else {
            try {
                //睡眠
                Thread.sleep(100);
                //自旋重试
                this.testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}