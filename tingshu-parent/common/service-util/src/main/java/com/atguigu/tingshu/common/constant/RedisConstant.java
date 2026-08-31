package com.atguigu.tingshu.common.constant;

public class RedisConstant {

    public static final String ALBUM_INFO_PREFIX = "album:info:";
    public static final String ALBUM_LOCK_SUFFIX = "album:lock:";

    //单位：秒
    //单位：秒 尝试获取锁的最大等待时间
    public static final long ALBUM_LOCK_EXPIRE_PX1 = 1;
    //单位：秒 锁的持有时间
    public static final long ALBUM_LOCK_EXPIRE_PX2 = 1;

    public static final long ALBUM_TIMEOUT = 1 * 60 * 60;
    // 商品如果在数据库中不存在那么会缓存一个空对象进去，但是这个对象是没有用的，所以这个对象的过期时间应该不能太长，
    // 如果太长会占用内存。
    // 定义变量，记录空对象的缓存过期时间
    public static final long ALBUM_TEMPORARY_TIMEOUT = 10 * 60;
    //  布隆过滤器使用！
    public static final String ALBUM_BLOOM_FILTER="album:bloom:filter";

    public static final String CACHE_INFO_PREFIX = "cache:info:";
    public static final String CACHE_LOCK_SUFFIX = "cache:lock:";

    //单位：秒
    //单位：秒 尝试获取锁的最大等待时间
    public static final long CACHE_LOCK_EXPIRE_PX1 = 1;
    //单位：秒 锁的持有时间
    public static final long CACHE_LOCK_EXPIRE_PX2 = 1;

    public static final long CACHE_TIMEOUT = 24 * 60 * 60;
    // 商品如果在数据库中不存在那么会缓存一个空对象进去，但是这个对象是没有用的，所以这个对象的过期时间应该不能太长，
    // 如果太长会占用内存。
    // 定义变量，记录空对象的缓存过期时间
    public static final long CACHE_TEMPORARY_TIMEOUT = 10 * 60;

    //用户登录
    public static final String USER_LOGIN_KEY_PREFIX = "user:login:";
    public static final String USER_LOGIN_REFRESH_KEY_PREFIX = "user:login:refresh:";
    public static final int USER_LOGIN_KEY_TIMEOUT = 60 * 60 * 24 * 100;
    public static final int USER_LOGIN_REFRESH_KEY_TIMEOUT = 60 * 60 * 24 * 365;

    public static final String RANKING_KEY_PREFIX = "ranking:";
    public static final String ALBUM_STAT_ENDTIME = "album:stat:endTime";
}
