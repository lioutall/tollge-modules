package com.tollge.modules.data.meilisearch.pojo;

import io.vertx.core.json.JsonObject;
import lombok.Data;

@Data
public class MultiSearchQuery extends SearchRequest {

    public JsonObject federationOptions;

    public String indexUid;

}
