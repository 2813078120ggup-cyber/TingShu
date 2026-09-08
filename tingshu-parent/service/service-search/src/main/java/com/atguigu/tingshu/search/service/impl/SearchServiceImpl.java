package com.atguigu.tingshu.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import com.alibaba.fastjson.JSON;
import com.atguigu.tingshu.album.client.AlbumInfoFeignClient;
import com.atguigu.tingshu.album.client.CategoryFeignClient;
import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.common.util.PinYinUtils;
import com.atguigu.tingshu.model.album.*;
import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import com.atguigu.tingshu.model.search.AttributeValueIndex;
import com.atguigu.tingshu.model.search.SuggestIndex;
import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.query.search.AlbumIndexQuery;
import com.atguigu.tingshu.search.repository.AlbumInfoIndexRepository;
import com.atguigu.tingshu.search.repository.SuggestIndexRepository;
import com.atguigu.tingshu.search.service.SearchService;
import com.atguigu.tingshu.user.client.UserInfoFeignClient;
import com.atguigu.tingshu.vo.search.AlbumInfoIndexVo;
import com.atguigu.tingshu.vo.search.AlbumSearchResponseVo;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


@Slf4j
@Service
@SuppressWarnings({"all"})
public class SearchServiceImpl implements SearchService {
    
    @Autowired
    private AlbumInfoFeignClient albumInfoFeignClient;
    @Autowired
    private CategoryFeignClient categoryFeignClient;
    @Autowired
    private UserInfoFeignClient userInfoFeignClient;
    @Autowired
    private AlbumInfoIndexRepository albumInfoIndexRepository;
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    @Autowired
    private SuggestIndexRepository suggestIndexRepository;
    @Autowired
    private RedisTemplate redisTemplate;
    
    // 根据专辑id实现上架
    /*@Override
    public void upperAlbum(Long albumId) {
        // 1. 调用5个远程调用接口，得到返回数据
        // 1.1 根据专辑id获取专辑信息
        Result<AlbumInfo> albumInfoResult = albumInfoFeignClient.getAlbumInfo(albumId);
        AlbumInfo albumInfo = albumInfoResult.getData();
        
        *//*if (albumInfo == null) {
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }*//*
        Assert.notNull(albumInfo, "专辑为空");  // 断言
        
        // 1.2 从专辑信息获取三级分类id，根据三级分类id获取一级和二级分类数据
        Long category3Id = albumInfo.getCategory3Id();
        Result<BaseCategoryView> categoryViewResult = categoryFeignClient.getCategoryView(category3Id);
        BaseCategoryView baseCategoryView = categoryViewResult.getData();
        Assert.notNull(baseCategoryView, "分类为空");  // 断言
        
        // 1.3 根据专辑id获取四个统计数据
        Result<Map<String, Object>> albumInfoStatResult = albumInfoFeignClient.getAlbumInfoStat(albumId);
        Map<String, Object> map = albumInfoStatResult.getData();
        //Integer playStatNum  = (Integer)map.get("playStatNum");
        
        // 1.4 根据专辑id获取标签名称和标签值数据列表
        Result<List<AlbumAttributeValue>> albumAttributeValueResult = albumInfoFeignClient.findAlbumAttributeValue(albumId);
        List<AlbumAttributeValue> albumAttributeValues = albumAttributeValueResult.getData();
        Assert.notNull(albumAttributeValues, "标签为空");  // 断言
        
        
        // 1.5 从专辑信息获取userId，根据userId获取用户信息
        Long userId = albumInfo.getUserId();
        Result<UserInfoVo> userInfoResult = userInfoFeignClient.getUserInfoVo(userId);
        UserInfoVo userInfo = userInfoResult.getData();
        Assert.notNull(userInfo, "用户为空");  // 断言
        
        
        // 2. 将五个远程调用接口获取数据封装到AlbumInfoIndex实体类
        AlbumInfoIndex albumInfoIndex = new AlbumInfoIndex();
        // 先封装albumInfo
        BeanUtils.copyProperties(albumInfo, albumInfoIndex);
        // 封装baseCategoryView的一级、二级、三级分类
        Long category1Id = baseCategoryView.getCategory1Id();
        Long category2Id = baseCategoryView.getCategory2Id();
        albumInfoIndex.setCategory1Id(category1Id);
        albumInfoIndex.setCategory2Id(category2Id);
        albumInfoIndex.setCategory3Id(category3Id);
        // 封装统计信息4
        // TODO: 为了测试方便采用随机数，实际开发中应该根据业务逻辑生成统计信息
        int num1 = new Random().nextInt(1000);
        int num2 = new Random().nextInt(100);
        int num3 = new Random().nextInt(50);
        int num4 = new Random().nextInt(300);
        albumInfoIndex.setPlayStatNum(num1);
        albumInfoIndex.setSubscribeStatNum(num2);
        albumInfoIndex.setBuyStatNum(num3);
        albumInfoIndex.setCommentStatNum(num4);
        double hotScore = num1 * 0.2 + num2 * 0.3 + num3 * 0.4 + num4 * 0.1;
        // 设置热度排名
        albumInfoIndex.setHotScore(hotScore);
        // 封装标签名称和标签值 List<AlbumAttributeValue> albumAttributeValues
        // albumAttributeValues -> List<AttributeValueIndex>
        if (!CollectionUtils.isEmpty(albumAttributeValues)) {
            List<AttributeValueIndex> attributeValueIndexList = albumAttributeValues.stream().map(item -> {
                AttributeValueIndex attributeValueIndex = new AttributeValueIndex();
                BeanUtils.copyProperties(item, attributeValueIndex);
                return attributeValueIndex;
            }).toList();
            albumInfoIndex.setAttributeValueIndexList(attributeValueIndexList);
        }
        
        // 封装用户昵称
        albumInfoIndex.setAnnouncerName(userInfo.getNickname());
        
        
        // 3. 调用AlbumInfoIndexRepository的save方法实现添加
        albumInfoIndexRepository.save(albumInfoIndex);
        
    }*/
    
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    
    // 线程池并行上传
    @Override
    public void upperAlbum(Long albumId) {
        AlbumInfoIndex albumInfoIndex = new AlbumInfoIndex();
        
        // 根据专辑id获取专辑信息
        CompletableFuture<AlbumInfo> completableFuture1 =
                CompletableFuture.supplyAsync(() -> {
                    Result<AlbumInfo> albumInfoResult = albumInfoFeignClient.getAlbumInfo(albumId);
                    AlbumInfo albumInfo = albumInfoResult.getData();
                    Assert.notNull(albumInfo, "专辑为空");
                    
                    // 封装到albumINfoIndex
                    BeanUtils.copyProperties(albumInfo, albumInfoIndex);
                    return albumInfo;
                }, threadPoolExecutor);
        
        // 从专辑信息获取三级分类id，根据三级分类id获取一级和二级分类数据
        // 在获取专辑信息后执行
        CompletableFuture<Void> completableFuture2 =
                completableFuture1.thenAcceptAsync((albumInfo) -> {
                    // 获取3级分类id
                    Long category3Id = albumInfo.getCategory3Id();
                    Result<BaseCategoryView> categoryViewResult = categoryFeignClient.getCategoryView(category3Id);
                    BaseCategoryView baseCategoryView = categoryViewResult.getData();
                    Assert.notNull(baseCategoryView, "分类为空");
                    
                    // 封装到albumInfoIndex
                    Long category1Id = baseCategoryView.getCategory1Id();
                    Long category2Id = baseCategoryView.getCategory2Id();
                    albumInfoIndex.setCategory1Id(category1Id);
                    albumInfoIndex.setCategory2Id(category2Id);
                    albumInfoIndex.setCategory3Id(category3Id);
                    
                }, threadPoolExecutor);
        
        // 根据专辑id获取四个统计数据
        
        
        // 获取专辑标签数据
        CompletableFuture<Void> completableFuture3 = CompletableFuture.runAsync(() -> {
            Result<List<AlbumAttributeValue>> albumAttributeValueResult = albumInfoFeignClient.findAlbumAttributeValue(albumId);
            
            List<AlbumAttributeValue> albumAttributeValues = albumAttributeValueResult.getData();
            if (!CollectionUtils.isEmpty(albumAttributeValues)) {
                List<AttributeValueIndex> attributeValueIndexList = albumAttributeValues.stream().map(item -> {
                    AttributeValueIndex attributeValueIndex = new AttributeValueIndex();
                    BeanUtils.copyProperties(item, attributeValueIndex);
                    return attributeValueIndex;
                }).toList();
                albumInfoIndex.setAttributeValueIndexList(attributeValueIndexList);
            }
            Assert.notNull(albumAttributeValues, "标签为空");
        }, threadPoolExecutor);
        
        // 获取用户信息
        CompletableFuture<Void> completableFuture4 = completableFuture1.thenAcceptAsync(albumInfo -> {
            Long userId = albumInfo.getUserId();
            log.info("获取用户信息, albumId: {}, userId: {}", albumId, userId);
            Result<UserInfoVo> userInfoResult = userInfoFeignClient.getUserInfoVo(userId);
            log.info("用户信息返回: code={}, message={}, data={}", userInfoResult.getCode(), userInfoResult.getMessage(), userInfoResult.getData());
            UserInfoVo userInfo = userInfoResult.getData();
            Assert.notNull(userInfo, "用户为空, userId=" + userId);
            
            // 封装到albumInfoIndex
            albumInfoIndex.setAnnouncerName(userInfo.getNickname());
        }, threadPoolExecutor);
        
        //  赋值初始化统计信息：
        int playStatNum = new Random().nextInt(100000);
        int subscribeStatNum = new Random().nextInt(100000000);
        int buyStatNum = new Random().nextInt(10000000);
        int commentStatNum = new Random().nextInt(1000000000);
        albumInfoIndex.setPlayStatNum(playStatNum);
        albumInfoIndex.setSubscribeStatNum(subscribeStatNum);
        albumInfoIndex.setBuyStatNum(buyStatNum);
        albumInfoIndex.setCommentStatNum(commentStatNum);
        
        // 等待所有 CompletableFuture 执行完成
        CompletableFuture.allOf(completableFuture1, completableFuture2, completableFuture3, completableFuture4).join();
        
        // 调用方法添加到es
        albumInfoIndexRepository.save(albumInfoIndex);
        
        // 添加数据到suggestinfo索引库，后面实现自动补全功能
        SuggestIndex suggestIndex = new SuggestIndex();
        suggestIndex.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        suggestIndex.setTitle(albumInfoIndex.getAlbumTitle());
        suggestIndex.setKeyword(new Completion(new String[]{albumInfoIndex.getAlbumTitle()}));
        suggestIndex.setKeywordPinyin(new Completion(new String[]{PinYinUtils.toHanyuPinyin(albumInfoIndex.getAlbumTitle())}));
        suggestIndex.setKeywordSequence(new Completion(new String[]{PinYinUtils.getFirstLetter(albumInfoIndex.getAlbumTitle())}));
        this.suggestIndexRepository.save(suggestIndex);
    }
    
    // 根据专辑id实现下架
    @Override
    public void lowerAlbum(Long albumId) {
        albumInfoIndexRepository.deleteById(albumId);
    }
    
    
    // 根据关键词检索
    @SneakyThrows
    @Override
    public AlbumSearchResponseVo search(AlbumIndexQuery albumIndexQuery) {
        //  调用方法，构建dsl语句
        SearchRequest request = this.buildQueryDsl(albumIndexQuery);
        // 调用elasticsearchClient的search方法执行检索
        SearchResponse<AlbumInfoIndex> response = elasticsearchClient.search(request, AlbumInfoIndex.class);
        // 把最终结果封装到AlbumSearchResponseVo
        AlbumSearchResponseVo albumSearchResponseVo = this.parseSearchResult(response);
        // 设置当前页
        albumSearchResponseVo.setPageSize(albumIndexQuery.getPageSize());
        albumSearchResponseVo.setPageNo(albumIndexQuery.getPageNo());
        long totalPages = (albumSearchResponseVo.getTotal() + albumIndexQuery.getPageSize() - 1) / albumIndexQuery.getPageSize();
        albumSearchResponseVo.setTotalPages(totalPages);
        return albumSearchResponseVo;
    }
    
    // 把es查询返回SearchResponse封装到AlbumSearchResponseVo
    private AlbumSearchResponseVo parseSearchResult(SearchResponse<AlbumInfoIndex> searchResponse) {
        // 创建SearchRequest
        /*SearchRequest.Builder requestBuilder = new SearchRequest.Builder();
        // requestBuilder构建dsl语句
        // requestBuilder.build()构建查询对象返回SearchRequest
        SearchRequest searchRequest = requestBuilder.build();*/
        //  创建对象
        AlbumSearchResponseVo searchResponseVo = new AlbumSearchResponseVo();
        //  private List<AlbumInfoIndexVo> list = new ArrayList<>();
        //  private Long total;//总记录数
        //  获取数据
        HitsMetadata<AlbumInfoIndex> hits = searchResponse.hits();
        //  总记录数
        searchResponseVo.setTotal(hits.total().value());
        //  获取数据
        List<Hit<AlbumInfoIndex>> subHist = hits.hits();
        //  判断
        if (!CollectionUtils.isEmpty(subHist)) {
            //  循环遍历.
            List<AlbumInfoIndexVo> list = subHist.stream().map(albumInfoIndexHit -> {
                //  创建对象
                AlbumInfoIndexVo albumInfoIndexVo = new AlbumInfoIndexVo();
                AlbumInfoIndex albumInfoIndex = albumInfoIndexHit.source();
                //  进行赋值
                BeanUtils.copyProperties(albumInfoIndex, albumInfoIndexVo);
                //  判断用户是否根据关键词进行检索.
                if (null != albumInfoIndexHit.highlight().get("albumTitle")) {
                    //  获取高亮数据
                    String albumTitle = albumInfoIndexHit.highlight().get("albumTitle").get(0);
                    //  赋值高亮数据
                    albumInfoIndexVo.setAlbumTitle(albumTitle);
                }
                //  返回数据
                return albumInfoIndexVo;
            }).collect(Collectors.toList());
            //  赋值
            searchResponseVo.setList(list);
        }
        //  返回数据
        return searchResponseVo;
    }
    
    // 构建查询dsl语句，最终返回查询对象
    private SearchRequest buildQueryDsl(AlbumIndexQuery albumIndexQuery) {
        //  检索入口： 关键词
        String keyword = albumIndexQuery.getKeyword();
        SearchRequest.Builder requestBuilder = new SearchRequest.Builder();
        //  {query - bool }
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        //  {query - bool - should - match}
        //  MatchQuery.Builder matchQuery = new MatchQuery.Builder();
        //  matchQuery.field("albumTitle").query(keyword);
        //  boolQuery.should(f-> f.match(matchQuery.build()));
        if (!StringUtils.isEmpty(keyword)) {
            //  {query - bool - should - match}
            boolQuery.should(f -> f.match(s -> s.field("albumTitle").query(keyword)));
            boolQuery.should(f -> f.match(s -> s.field("albumIntro").query(keyword)));
            //  高亮
            requestBuilder.highlight(h -> h.fields("albumTitle",
                    //                    f->f.preTags("<font color:red>").postTags("</font>")
                    f -> f.preTags("<span style=color:red>").postTags("</span>")
            ));
        }
        
        //  入口：分类Id  复制小括号，写死右箭头，落地大括号
        //  一级分类Id
        Long category1Id = albumIndexQuery.getCategory1Id();
        if (!StringUtils.isEmpty(category1Id)) {
            boolQuery.filter(f -> f.term(s -> s.field("category1Id").value(category1Id)));
        }
        //  二级分类Id
        Long category2Id = albumIndexQuery.getCategory2Id();
        if (!StringUtils.isEmpty(category2Id)) {
            boolQuery.filter(f -> f.term(s -> s.field("category2Id").value(category2Id)));
        }
        //  三级分类Id
        Long category3Id = albumIndexQuery.getCategory3Id();
        if (!StringUtils.isEmpty(category3Id)) {
            boolQuery.filter(f -> f.term(s -> s.field("category3Id").value(category3Id)));
        }
        
        //  根据属性Id 检索 前端传递数据的时候 属性Id:属性值Id 属性Id:属性值Id
        List<String> attributeList = albumIndexQuery.getAttributeList();
        //  判断集合不为空
        if (!CollectionUtils.isEmpty(attributeList)) {
            //  循环遍历.
            for (String attribute : attributeList) {
                //  需要使用 : 分割
                String[] split = attribute.split(":");
                //  判断
                if (null != split && split.length == 2) {
                    //  创建nestedQuery 对象.
                    NestedQuery nestedQuery = NestedQuery.of(f -> f.path("attributeValueIndexList")
                            .query(q -> q.bool(
                                    m -> m.must(s -> s.match(
                                                    a -> a.field("attributeValueIndexList.attributeId").query(split[0])
                                            ))
                                            .must(s -> s.match(
                                                    a -> a.field("attributeValueIndexList.valueId").query(split[1])
                                            ))
                            ))
                    );
                    boolQuery.filter(f -> f.nested(nestedQuery));
                }
            }
        }
        
        //  排序 分页 高亮 排序（综合排序[1:desc] 播放量[2:desc] 发布时间[3:desc]；asc:升序 desc:降序）
        String order = albumIndexQuery.getOrder();
        //  定义一个排序字段
        String orderField = "";
        //  定义一个排序规则
        String sort = "";
        //  判断
        if (!StringUtils.isEmpty(order)) {
            //  分割数据
            String[] split = order.split(":");
            //  判断这个数组
            if (null != split && split.length == 2) {
                switch (split[0]) {
                    case "1":
                        orderField = "hotScore";
                        break;
                    case "2":
                        orderField = "playStatNum";
                        break;
                    case "3":
                        orderField = "createTime";
                        break;
                }
                sort = split[1];
            }
            //  判断 desc SortOrder.Desc  asc SortOrder.Asc
            String finalSort = sort;
            String finalOrderField = orderField;
            requestBuilder.sort(f -> f.field(o -> o.field(finalOrderField).order("asc".equals(finalSort) ? SortOrder.Asc : SortOrder.Desc)));
        } else {
            //  默认排序规则 _score
            requestBuilder.sort(f -> f.field(o -> o.field("_score").order(SortOrder.Desc)));
        }
        //  字段选择
        requestBuilder.source(s -> s.filter(f -> f.excludes("attributeValueIndexList")));
        //  分页： (pageNo-1)*pageSize()
        Integer from = (albumIndexQuery.getPageNo() - 1) * albumIndexQuery.getPageSize();
        requestBuilder.from(from);
        requestBuilder.size(albumIndexQuery.getPageSize());
        
        //  {query }
        //  GET /albuminfo/_search
        requestBuilder.index("albuminfo").query(f -> f.bool(boolQuery.build()));
        //  创建对象
        SearchRequest searchRequest = requestBuilder.build();
        System.out.println("dsl:\t" + searchRequest.toString());
        //  返回
        return searchRequest;
    }
    
    // 根据一级分类Id获取数据
    @Override
    public List<Map<String, Object>> channel(Long category1Id) {
        //  根据一级分类Id 获取到置顶数据集合
        Result<List<BaseCategory3>> baseCategory3ListResult = categoryFeignClient.findTopBaseCategory3(category1Id);
        //  获取数据
        List<BaseCategory3> baseCategory3List = baseCategory3ListResult.getData();
        //  建立对应关系 key = 三级分类Id value = 三级分类对象
        Map<Long, BaseCategory3> category3IdToMap = baseCategory3List.stream().collect(Collectors.toMap(BaseCategory3::getId, baseCategory3 -> baseCategory3));
        //  获取到三级分类Id 集合
        List<Long> idList = baseCategory3List.stream().map(BaseCategory3::getId).collect(Collectors.toList());
        //  将这个idList进行转换
        List<FieldValue> valueList = idList.stream().map(id -> FieldValue.of(id)).collect(Collectors.toList());
        //  调用查询方法:
        SearchRequest.Builder request = new SearchRequest.Builder();
        request.index("albuminfo").query(q -> q.terms(f -> f.field("category3Id").terms(new TermsQueryField.Builder().value(valueList).build())));
        request.aggregations("groupByCategory3IdAgg", a -> a.terms(t -> t.field("category3Id"))
                .aggregations("topSixHotScoreAgg", a1 -> a1.topHits(s -> s.size(6).sort(sort -> sort.field(f -> f.field("hotScore").order(SortOrder.Desc))))));
        //  获取到查询结果集
        SearchResponse<AlbumInfoIndex> searchResponse = null;
        try {
            searchResponse = elasticsearchClient.search(request.build(), AlbumInfoIndex.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //  声明集合
        List<Map<String, Object>> result = new ArrayList<>();
        //  从聚合中获取数据
        Aggregate groupByCategory3IdAgg = searchResponse.aggregations().get("groupByCategory3IdAgg");
        groupByCategory3IdAgg.lterms().buckets().array().forEach(item -> {
            //  创建集合数据
            List<AlbumInfoIndex> albumInfoIndexList = new ArrayList<>();
            //  获取三级分类Id 对象
            long category3Id = item.key();
            //  获取要置顶的集合数据
            Aggregate topSixHotScoreAgg = item.aggregations().get("topSixHotScoreAgg");
            //  循环遍历获取聚合中的数据
            topSixHotScoreAgg.topHits().hits().hits().forEach(hit -> {
                //  获取到source 的json 字符串数据
                String json = hit.source().toString();
                //  将json 字符串转换为AlbumInfoIndex 对象
                AlbumInfoIndex albumInfoIndex = JSON.parseObject(json, AlbumInfoIndex.class);
                //  将对象添加到集合中
                albumInfoIndexList.add(albumInfoIndex);
            });
            //  声明一个map 集合数据
            Map<String, Object> map = new HashMap<>();
            //  存储根据三级分类Id要找到的三级分类
            map.put("baseCategory3", category3IdToMap.get(category3Id));
            //  存储所有的专辑集合数据
            map.put("list", albumInfoIndexList);
            //  将map 添加到集合中
            result.add(map);
        });
        //  返回数据
        return result;
    }
    
    
    //  自动补全
    
    @Override
    public List<String> completeSuggest(String keyword) {
        //  Java 动态生成dsl 语句.
        SearchRequest.Builder searchRequest = new SearchRequest.Builder();
        searchRequest.index("suggestinfo").suggest(
                s -> s.suggesters("suggestionKeyword", f -> f.prefix(keyword).completion(
                                c -> c.field("keyword").skipDuplicates(true).size(10)
                                        .fuzzy(
                                                z -> z.fuzziness("auto"))
                        ))
                        .suggesters("suggestionkeywordPinyin", f -> f.prefix(keyword).completion(
                                c -> c.field("keywordPinyin").skipDuplicates(true).size(10)
                                        .fuzzy(z -> z.fuzziness("auto"))
                        ))
                        .suggesters("suggestionkeywordSequence", f -> f.prefix(keyword).completion(
                                c -> c.field("keywordSequence").skipDuplicates(true).size(10)
                                        .fuzzy(z -> z.fuzziness("auto"))
                        ))
        );
        //  获取查询结果
        SearchResponse<SuggestIndex> searchResponse = null;
        try {
            searchResponse = elasticsearchClient.search(searchRequest.build(), SuggestIndex.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //  获取到结果集,数据转换. set集合无序不重复? 1.hashCode(); 2.equals();   为什么 底层hashMap !map.key=value map.value=new Object();
        HashSet<String> titleSet = new HashSet<>();
        titleSet.addAll(this.parseResultData(searchResponse, "suggestionKeyword"));
        titleSet.addAll(this.parseResultData(searchResponse, "suggestionkeywordPinyin"));
        titleSet.addAll(this.parseResultData(searchResponse, "suggestionkeywordSequence"));
        
        //  判断：
        if (titleSet.size() < 10) {
            //  使用查询数据的方式来填充集合数据，让这个提示信息够10条数据.
            SearchResponse<SuggestIndex> response = null;
            try {
                response = elasticsearchClient.search(s -> s.index("suggestinfo")
                                .query(f -> f.match(m -> m.field("title").query(keyword)))
                        , SuggestIndex.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //  从查询结果集中获取数据
            for (Hit<SuggestIndex> hit : response.hits().hits()) {
                //  获取数据结果
                SuggestIndex suggestIndex = hit.source();
                //  获取titile
                titleSet.add(suggestIndex.getTitle());
                //  判断当前这个结合的长度.
                if (titleSet.size() == 10) {
                    break;
                }
            }
        }
        //  返回数据
        return new ArrayList<>(titleSet);
    }
    
    // 处理聚合结果集
    
    private Collection<String> parseResultData(SearchResponse<SuggestIndex> searchResponse, String suggestionKeyword) {
        //  创建集合
        List<String> suggestList = new ArrayList<>();
        Map<String, List<Suggestion<SuggestIndex>>> groupBySuggestionListAggMap = searchResponse.suggest();
        groupBySuggestionListAggMap.get(suggestionKeyword).forEach(item -> {
            CompletionSuggest<SuggestIndex> completionSuggest = item.completion();
            completionSuggest.options().forEach(it -> {
                SuggestIndex suggestIndex = it.source();
                suggestList.add(suggestIndex.getTitle());
            });
        });
        //  返回集合列表
        return suggestList;
    }
    
    //  更新排行榜
    @Override
    public void updateLatelyAlbumRanking() {
        //  先获取到所有一级分类数据
        Result<List<BaseCategory1>> baseCategory1Result = categoryFeignClient.findAllCategory1();
        //  获取集合
        Assert.notNull(baseCategory1Result, "一级分类结果集为空");
        List<BaseCategory1> baseCategory1List = baseCategory1Result.getData();
        Assert.notNull(baseCategory1List, "一级分类集合为空");
        //  循环遍历
        for (BaseCategory1 baseCategory1 : baseCategory1List) {
            //  baseCategory1.getId()
            //  Hash数据结构： hset key field value;  key=category1Id field = 热度/播放量/订阅量/ value=热度data/播放量data;
            String[] rankingDimensionArray = new String[]{"hotScore", "playStatNum", "subscribeStatNum", "buyStatNum", "commentStatNum"};
            //  排行榜数据从哪里来的? es 中!
            for (String ranging : rankingDimensionArray) {
                //  执行dsl语句 获取到结果集.
                SearchResponse<AlbumInfoIndex> response = null;
                try {
                    response = elasticsearchClient.search(f -> f.index("albuminfo")
                            .query(q -> q.term(t -> t.field("category1Id").value(baseCategory1.getId())))
                            .sort(s -> s.field(d -> d.field(ranging).order(SortOrder.Desc)))
                            .size(10), AlbumInfoIndex.class);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                //  获取到当前执行结果集，将数据存储到缓存：
                List<AlbumInfoIndex> albumInfoIndexList = response.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
                //  使用hash 数据结构 hset key field value;
                String rangKey = RedisConstant.RANKING_KEY_PREFIX + baseCategory1.getId();
                this.redisTemplate.boundHashOps(rangKey).put(ranging, albumInfoIndexList);
                //  this.redisTemplate.opsForHash().put(rangKey,ranging,albumInfoIndexList);
            }
        }
    }
    
    // 获取排行榜列表
    
    @Override
    public List<AlbumInfoIndexVo> findRankingList(Long category1Id, String dimension) {
        return (List<AlbumInfoIndexVo>) redisTemplate.boundHashOps(RedisConstant.RANKING_KEY_PREFIX + category1Id).get(dimension);
    }
}
