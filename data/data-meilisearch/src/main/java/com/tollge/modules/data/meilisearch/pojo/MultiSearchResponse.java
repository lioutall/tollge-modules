package com.tollge.modules.data.meilisearch.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MultiSearchResponse implements Serializable {
    private List<MultiSearchResult> results;
}
