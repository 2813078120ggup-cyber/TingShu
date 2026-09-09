package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.cache.TingShuCache;
import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.login.TingShuLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.atguigu.tingshu.vo.album.AlbumStatVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "专辑管理")
@RestController
@RequestMapping("api/album/albumInfo")
@SuppressWarnings({"all"})
public class AlbumInfoApiController {
    
    @Autowired
    private AlbumInfoService albumInfoService;
    @Autowired
    private RedissonClient redisson;
    @Autowired
    private RedissonClient redissonClient;
    
    //Request URL: http://localhost/api/album/albumInfo/saveAlbumInfo
    //Request Method: POST
    
    // 保存专辑信息
    @PostMapping("/saveAlbumInfo")
    public Result saveAlbumInfo(@RequestBody @Validated AlbumInfoVo albumInfoVo) {
        albumInfoService.saveAlbumInfo(albumInfoVo);
        return Result.ok();
        
    }
    
    
    // 查询专辑列表
    @TingShuLogin
    @Operation(summary = "获取当前用户专辑分页列表")
    @PostMapping("findUserAlbumPage/{page}/{limit}")
    public Result findUserAlbumPage(@PathVariable Long page,
                                    @PathVariable Long limit,
                                    @RequestBody AlbumInfoQuery albumInfoQuery
    ) {
        albumInfoQuery.setUserId(1L);
        // 创建page对象，传递当前页和每页记录数
        Page<AlbumListVo> pageParam = new Page<>(page, limit);
        // 调用service方法
        IPage<AlbumListVo> pageModel = albumInfoService.selectAlbumPage(pageParam, albumInfoQuery);
        return Result.ok(pageModel);
    }
    
    //获取当前用户全部专辑列表
    //Request URL: http://localhost/api/album/albumInfo/findUserAllAlbumList
    //Request Method: GET
    @Operation(summary = "获取当前用户全部专辑列表")
    @GetMapping("findUserAllAlbumList")
    public Result findUserAllAlbumList() {
        //	调用服务层方法
        Long userId = 1L; //todo 获取当前用户id
        List<AlbumInfo> list = albumInfoService.findUserAllAlbumList(userId);
        return Result.ok(list);
    }
    
    
    // 删除专辑
    // Request URL: http://localhost/api/album/albumInfo/removeAlbumInfo/1593
    // Request Method: DELETE
    @DeleteMapping("removeAlbumInfo/{albumId}")
    public Result removeAlbumInfo(@PathVariable Long albumId) {
        albumInfoService.removeAlbumInfo(albumId);
        return Result.ok();
    }
    
    
    // 修改专辑信息
    // 根据专辑id获取专辑数据
    //Request URL: http://localhost/api/album/category/getBaseCategoryList
    //Request Method: GET
    @TingShuCache(prefix = RedisConstant.ALBUM_INFO_PREFIX)
    @GetMapping("getAlbumInfo/{albumId}")
    public Result<AlbumInfo> getAlbumInfo(@PathVariable Long albumId) {
        // 添加布隆过滤器判断
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConstant.ALBUM_BLOOM_FILTER);
        if (!bloomFilter.contains(albumId)) {
            throw new GuiguException(228, "专辑不存在" + albumId);
        }
        AlbumInfo albumInfo = albumInfoService.getAlbumInfo(albumId);
        return Result.ok(albumInfo);
    }
        
        // 修改专辑信息
        //Request URL: http://localhost/api/album/albumInfo/updateAlbumInfo/1594
        //Request Method: PUT
        @PutMapping("updateAlbumInfo/{albumId}")
        public Result updateAlbumInfo (@PathVariable Long albumId, @RequestBody @Validated AlbumInfoVo albumInfoVo){
            albumInfoService.updateAlbumInfo(albumId, albumInfoVo);
            return Result.ok();
        }
        
        
        // 远程调用：根据专辑id获得4统计数据
        @GetMapping("getAlbumInfoStat/{albumId}")
        public Result<Map<String, Object>> getAlbumInfoStat (@PathVariable Long albumId){
            Map<String, Object> map = albumInfoService.getAlbumInfoStat(albumId);
            return Result.ok(map);
        }
        
        //根据专辑Id 获取到专辑属性列表
        
        /**
         * 根据专辑Id 获取到专辑属性列表
         *
         * @param albumId
         * @return
         */
        @Operation(summary = "获取专辑属性值列表")
        @GetMapping("findAlbumAttributeValue/{albumId}")
        public Result<List<AlbumAttributeValue>> findAlbumAttributeValue (@PathVariable Long albumId){
            //	获取到专辑属性集合
            List<AlbumAttributeValue> albumAttributeValueList = albumInfoService.findAlbumAttributeValueByAlbumId(albumId);
            return Result.ok(albumAttributeValueList);
        }
        
        
        /**
         * 根据专辑Id 获取到统计信息
         *
         * @param albumId
         * @return
         */
        @Operation(summary = "获取到专辑统计信息")
        @GetMapping("/getAlbumStatVo/{albumId}")
        public Result getAlbumStatVo (@PathVariable Long albumId){
            //	获取服务层方法
            AlbumStatVo albumStatVo = this.albumInfoService.getAlbumStatVoByAlbumId(albumId);
            return Result.ok(albumStatVo);
        }
        
        
    }

