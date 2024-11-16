package com.tollge.modules.data.meilisearch.pojo;

import lombok.Data;

import java.util.List;

@Data
public class SearchRequest {

    // 查询字符串
    private String q;
    // 跳过的文档数量
    private Integer offset;
    // 返回的最大文档数量
    private Integer limit;
    // 每页返回的最大文档数量
    private Integer hitsPerPage;
    // 请求特定页面的结果
    private Integer page;
    // 按属性值过滤查询
    // 语法: https://www.meilisearch.com/docs/reference/api/search#filter
    private String filter;
    // 显示每个facet的匹配数量
    private List<String> facets;
    // 返回文档中显示的属性
    private List<String> attributesToRetrieve;
    // 需裁剪的属性值
    private List<String> attributesToCrop;
    // 裁剪后最大字数长度
    private Integer cropLength;
    // 裁剪边界标记
    private String cropMarker;
    // 高亮显示的匹配项
    private List<String> attributesToHighlight;
    // 高亮开始标记
    private String highlightPreTag;
    // 高亮结束标记
    private String highlightPostTag;
    // 返回匹配项位置
    private Boolean showMatchesPosition;
    // 按属性值排序搜索结果
    private List<String> sort;
    // 查询词在文档中的匹配策略
    private String matchingStrategy;
    // 显示文档的全局排名分数
    private Boolean showRankingScore;
    // 添加详细的全局排名分数字段
    private Boolean showRankingScoreDetails;
    // 排名分数阈值，排除低排名结果
    private Number rankingScoreThreshold;
    // 限制搜索到指定属性
    private List<String> attributesToSearchOn;
    // 基于查询关键词和含义返回结果
    private Object hybrid;
    // 使用自定义查询向量进行搜索
    private List<Double> vector;
    // 返回文档向量数据
    private Boolean retrieveVectors;
    // 显式用于查询的语言
    private List<String> locales;
}

