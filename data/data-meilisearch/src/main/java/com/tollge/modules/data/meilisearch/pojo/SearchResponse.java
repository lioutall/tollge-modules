package com.tollge.modules.data.meilisearch.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class SearchResponse implements Serializable {

    // 查询结果
    private List<Map<String, Object>> hits;
    // 跳过的文档数量
    private Long offset;
    // 取出的文档数量
    private Integer limit;
    // 估计的总匹配数量
    private Long estimatedTotalHits;
    // 全部匹配的总数量
    private Long totalHits;
    // 全部搜索结果页数
    private Integer totalPages;
    // 每页的结果数量
    private Integer hitsPerPage;
    // 当前搜索结果页面
    private Integer page;
    // 查询的处理时间（毫秒）
    private Long processingTimeMs;
    // 发起响应的查询
    private String query;

}
