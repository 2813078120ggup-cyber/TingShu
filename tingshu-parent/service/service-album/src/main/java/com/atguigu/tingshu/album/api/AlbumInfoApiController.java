package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "专辑管理")
@RestController
@RequestMapping("api/album/albumInfo")
@SuppressWarnings({"all"})
public class AlbumInfoApiController {
    
    @Autowired
    private AlbumInfoService albumInfoService;
    
    //Request URL: http://localhost/api/album/albumInfo/saveAlbumInfo
    //Request Method: POST
    
    // 保存专辑信息
    @PostMapping("/saveAlbumInfo")
    public Result saveAlbumInfo(@RequestBody @Validated AlbumInfoVo albumInfoVo) {
        albumInfoService.saveAlbumInfo(albumInfoVo);
        return Result.ok();
        
    }
    
    
    // 查询专辑列表
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
    @GetMapping("getAlbumInfo/{albumId}")
    public Result<AlbumInfo> getAlbumInfo(@PathVariable Long albumId) {
        AlbumInfo albumInfo = albumInfoService.getAlbumInfo(albumId);
        return Result.ok(albumInfo);
    }
    
    // 修改专辑信息
    //Request URL: http://localhost/api/album/albumInfo/updateAlbumInfo/1594
    //Request Method: PUT
    @PutMapping("updateAlbumInfo/{albumId}")
    public Result updateAlbumInfo(@PathVariable Long albumId, @RequestBody @Validated AlbumInfoVo albumInfoVo) {
        albumInfoService.updateAlbumInfo(albumId, albumInfoVo);
        return Result.ok();
    }
}

