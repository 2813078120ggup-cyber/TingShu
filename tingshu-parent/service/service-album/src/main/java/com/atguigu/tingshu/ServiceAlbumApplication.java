package com.atguigu.tingshu;

import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.model.album.AlbumInfo;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.List;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ServiceAlbumApplication implements CommandLineRunner {
    
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private AlbumInfoService albumInfoService;
    
    public static void main(String[] args) {
        SpringApplication.run(ServiceAlbumApplication.class, args);
    }
    
    /**
     * Springboot应用初始化后会执行一次该方法
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        //1. 初始化布隆过滤器
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConstant.ALBUM_BLOOM_FILTER);
        //2. 设置数据规模 误判率 预计统计元素数量为100000，期望误差率为0.01
        bloomFilter.tryInit(100000, 0.01);
        //3. 查询所有专辑id，把所有专辑id放到布隆过滤器中
        List<AlbumInfo> list = albumInfoService.list();
        for (AlbumInfo albumInfo : list) {
            // 专辑
            Long albumId = albumInfo.getId();
            // 把albumId放到布隆过滤器中
            bloomFilter.add(albumId);
        }
    }
}

