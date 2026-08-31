package com.atguigu.tingshu.vo.album;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户专辑列表信息")
public class AlbumListVo {

	@Schema(description = "专辑id")
	private Long albumId;

	@Schema(description = "标题")
	private String albumTitle;

	@Schema(description = "专辑封面原图，尺寸不固定，最大尺寸为960*960（像素）")
	private String coverUrl;

	@Schema(description = "专辑包含声音总数")
	private Integer includeTrackCount;

	@Schema(description = "专辑是否完结：0-否；1-完结；")
	private String isFinished;

	@Schema(description = "状态")
	private String status;

	@Schema(description = "播放量")
	private Integer playStatNum;

	@Schema(description = "订阅量")
	private Integer subscribeStatNum;

	@Schema(description = "购买量")
	private Integer buyStatNum;

	@Schema(description = "评论数")
	private Integer commentStatNum;

}