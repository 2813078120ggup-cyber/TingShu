package com.atguigu.tingshu.vo.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果包装
 *
 * @author itcast
 */
@Data
@Schema(description = "分页数据消息体")
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class PageVo<T> implements Serializable {

    @Schema(description = "总条目数", required = true)
    private Long total;

    @Schema(description = "页尺寸", required = true)
    private Long size;

    @Schema(description = "总页数", required = true)
    private Long pages;

    @Schema(description = "当前页码", required = true)
    private long current;

    @Schema(description = "数据列表", required = true)
    private List<T> records;

    public PageVo(IPage pageModel) {
        this.setRecords(pageModel.getRecords());
        this.setTotal(pageModel.getTotal());
        this.setSize(pageModel.getSize());
        this.setPages(pageModel.getPages());
        this.setCurrent(pageModel.getCurrent());
    }

    public PageVo(List<T> list, IPage pageModel) {
        this.setRecords(list);
        this.setTotal(pageModel.getTotal());
        this.setSize(pageModel.getSize());
        this.setPages(pageModel.getPages());
        this.setCurrent(pageModel.getCurrent());
    }

}
