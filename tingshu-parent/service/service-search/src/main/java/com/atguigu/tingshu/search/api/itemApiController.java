package com.atguigu.tingshu.search.api;

import com.atguigu.tingshu.common.login.TingShuLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.search.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "专辑详情管理")
@RestController
@RequestMapping("api/search/albumInfo")
@SuppressWarnings({"unchecked", "rawtypes"})
public class itemApiController {
    
    @Autowired
    private ItemService itemService;
    
    //Request URL: http://localhost/api/search/albumInfo/12
    //Request Method: GET
    /**
     * 根据专辑Id 获取详情数据
     *
     * @param albumId
     * @return
     */
    @TingShuLogin(required = false)
    @Operation(summary = "专辑详情")
    @GetMapping("{albumId}")
    public Result getItem(@PathVariable Long albumId) {
        // 获取到专辑详情数据
        Map<String, Object> result = this.itemService.getItem(albumId);
        // 返回数据
        return Result.ok(result);
    }
}

