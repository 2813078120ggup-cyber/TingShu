package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.login.TingShuLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "声音管理")
@RestController
@RequestMapping("api/album/trackInfo")
@SuppressWarnings({"all"})
public class TrackInfoApiController {
    
    @Autowired
    private TrackInfoService trackInfoService;
    @Autowired
    private VodService vodService;
    @Autowired
    private RedisTemplate redisTemplate;
    
    // 查询声音列表
    //Request URL: http://localhost/api/album/trackInfo/findUserTrackPage/1/10
    //Request Method: POST
    
    /**
     * 查看声音专辑列表
     *
     * @param page
     * @param limit
     * @param trackInfoQuery
     * @return
     */
    //@TingshuLogin(value = "123")
    @TingShuLogin(required = true)
    @Operation(summary = "获取当前用户声音分页列表")
    @PostMapping("findUserTrackPage/{page}/{limit}")
    public Result<IPage<TrackListVo>> findUserTrackPage(@Parameter(name = "page", description = "当前页面", required = true)
                                                        @PathVariable Long page,
                                                        @Parameter(name = "limit", description = "每页记录数", required = true)
                                                        @PathVariable Long limit,
                                                        @Parameter(name = "trackInfoQuery", description = "查询对象", required = false)
                                                        @RequestBody TrackInfoQuery trackInfoQuery,
                                                        HttpServletRequest request) {
        /*// 1. 从请求头获取token（前端传递）
        String token = request.getHeader("token");
        if (!StringUtils.hasText(token)) {
            throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
        }
        // 2. 根据token查询redis（redis的key是token）， 如果可以查询到是登录，查询不到则没有登录
        UserInfo userInfo = (UserInfo) redisTemplate.opsForValue().get(token);
        if (userInfo == null) {
            throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
        }*/
        //	设置当前用户Id
        //trackInfoQuery.setUserId(AuthContextHolder.getUserId());
        trackInfoQuery.setUserId(1L);
        //	创建对象
        Page<TrackListVo> trackListVoPage = new Page<>(page, limit);
        IPage<TrackListVo> trackListVoIPage = trackInfoService.findUserTrackPage(trackListVoPage, trackInfoQuery);
        //	返回数据
        return Result.ok(trackListVoIPage);
    }
    
    
    //Request URL: http://localhost/api/album/trackInfo/uploadTrack
    //Request Method: post
    
    /**
     * 上传声音
     *
     * @param file
     * @return
     */
    @Operation(summary = "上传声音")
    @PostMapping("uploadTrack")
    public Result<Map<String, Object>> uploadTrack(MultipartFile file) {
        //	调用服务层方法
        Map<String, Object> map = vodService.uploadTrack(file);
        return Result.ok(map);
    }
    
    
    // 保存声音
    
    /**
     * 保存声音
     *
     * @param trackInfoVo
     * @return
     */
    @Operation(summary = "新增声音")
    @PostMapping("saveTrackInfo")
    public Result saveTrackInfo(@RequestBody @Validated TrackInfoVo trackInfoVo) {
        //	调用服务层方法
        trackInfoService.saveTrackInfo(trackInfoVo, AuthContextHolder.getUserId());
        return Result.ok();
    }
    
    
    // 删除声音
    //Request URL: http://localhost/api/album/trackInfo/removeTrackInfo/51942
    //Request Method: DELETE
    
    /**
     * 删除声音
     *
     * @param id
     * @return
     */
    @Operation(summary = "删除声音信息")
    @DeleteMapping("removeTrackInfo/{id}")
    public Result removeTrackInfo(@PathVariable Long id) {
        //	调用服务层方法
        trackInfoService.removeTrackInfo(id);
        return Result.ok();
    }
    
    
    // 修改声音
    // 根据声音id回显声音信息
    //Request URL: http://localhost/api/album/trackInfo/getTrackInfo/51933
    //Request Method: GET
    
    /**
     * 根据Id 获取数据
     *
     * @param id
     * @return
     */
    @Operation(summary = "获取声音信息")
    @GetMapping("getTrackInfo/{id}")
    public Result<TrackInfo> getTrackInfo(@PathVariable Long id) {
        //	调用服务层方法
        TrackInfo trackInfo = trackInfoService.getById(id);
        return Result.ok(trackInfo);
    }
    
    // 修改声音
    //Request URL: http://localhost/api/album/trackInfo/updateTrackInfo/51943
    //Request Method: PUT
    
    /**
     * 保存修改声音数据
     *
     * @param id
     * @param trackInfoVo
     * @return
     */
    @Operation(summary = "修改声音")
    @PutMapping("updateTrackInfo/{id}")
    public Result updateById(@PathVariable Long id, @RequestBody @Validated TrackInfoVo trackInfoVo) {
        //	调用服务层方法
        trackInfoService.updateTrackInfo(id, trackInfoVo);
        return Result.ok();
    }
}

