package com.tollge.modules.data.meilisearch.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class MultiSearchResult extends SearchResponse implements Serializable {

    public String indexUid;

}
