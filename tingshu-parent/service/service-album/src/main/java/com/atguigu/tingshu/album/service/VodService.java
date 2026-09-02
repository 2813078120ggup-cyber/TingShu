package com.atguigu.tingshu.album.service;

import com.atguigu.tingshu.vo.album.TrackMediaInfoVo;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface VodService {
    
    // 上传声音
    Map<String, Object> uploadTrack(MultipartFile file);
    
    // 获取媒体文件信息
    TrackMediaInfoVo getTrackMediaInfo(@NotEmpty(message = "媒体文件Id不能为空") String mediaFileId);
    
    // 删除媒体文件
    void removeTrack(String mediaFileId);
}
