package com.atguigu.tingshu.vo.search;

import com.atguigu.tingshu.model.search.AttributeValueIndex;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

// Index = albuminfo , Type = _doc
//  7.8.0 版本
@Data
public class AlbumSearchVo {
    // 专辑Id
    @Id
    private Long id;

    //  es 中能分词的字段，这个字段数据类型必须是 text！keyword 不分词！
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String albumTitle;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String albumIntro;

//    @Field(type = FieldType.Keyword)
//    private String announcerName;

    //专辑封面
    @Field(type = FieldType.Keyword, index = false)
    private String coverUrl;

    //专辑包含声音总数
    @Field(type = FieldType.Long, index = false)
    private Integer includeTrackCount;

    //专辑是否完结：0-否；1-完结
    @Field(type = FieldType.Long, index = false)
    private String isFinished;

    //付费类型：免费、vip免费、付费
    @Field(type = FieldType.Keyword, index = false)
    private String payType;

    @Field(type = FieldType.Date,format = DateFormat.basic_date_time, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime; //

    @Field(type = FieldType.Long)
    private Long category1Id;

    @Field(type = FieldType.Long)
    private Long category2Id;

    @Field(type = FieldType.Long)
    private Long category3Id;

    //播放量
    @Field(type = FieldType.Long)
    private Integer playStatNum = 0;

    //  商品的热度！
    @Field(type = FieldType.Long)
    private Long hotScore = 0L;

    //专辑属性值
    // Nested 支持嵌套查询
    @Field(type = FieldType.Nested)
    private List<AttributeValueIndex> attributeValueIndexList;

}
