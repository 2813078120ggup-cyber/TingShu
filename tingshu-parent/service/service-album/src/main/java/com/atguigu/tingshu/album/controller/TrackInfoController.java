package com.atguigu.tingshu.album.controller;

import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "声音管理")
@RestController
@RequestMapping("api/album/trackInfo")
@SuppressWarnings({"all"})
public class TrackInfoController {
    
    @Autowired
    private TrackInfoService trackInfoService;
    @Autowired
    private VodService vodService;
    
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
    
}

