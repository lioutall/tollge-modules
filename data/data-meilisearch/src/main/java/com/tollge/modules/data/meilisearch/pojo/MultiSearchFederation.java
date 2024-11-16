package com.tollge.modules.data.meilisearch.pojo;

import lombok.Data;

@Data
public class MultiSearchFederation {

    // 跳过的文档数量
    private Integer offset;
    // 返回的最大文档数量
    private Integer limit;
}
