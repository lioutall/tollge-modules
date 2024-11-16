package com.tollge.modules.data.meilisearch.pojo;

import lombok.Data;

import java.util.List;

@Data
public class MultiSearchRequest {
    /**
     * 如果存在且不为 null，则返回一个列表，合并所有指定查询中的所有搜索结果
     */
    private MultiSearchFederation federation;

    /**
     * limit、offset、hitsPerPage 和 page
     * 这些选项与联合搜索不兼容。
     */
    private List<MultiSearchQuery> queries;
}
