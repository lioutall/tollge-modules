package test.meilisearch;

import com.tollge.common.annotation.mark.Biz;
import com.tollge.common.annotation.mark.Path;
import com.tollge.common.verticle.BizVerticle;
import com.tollge.modules.data.meilisearch.MyMeilisearch;
import com.tollge.modules.data.meilisearch.pojo.MultiSearchFederation;
import com.tollge.modules.data.meilisearch.pojo.MultiSearchQuery;
import com.tollge.modules.data.meilisearch.pojo.MultiSearchRequest;
import com.tollge.modules.data.meilisearch.pojo.SearchRequest;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Biz("biz://tt")
public class HttpBiz extends BizVerticle {

    @Path("/testMeiliAdd")
    public void testMeiliAdd(Message<JsonObject> msg) {
        JsonObject j = new JsonObject();
        j.put("id", 1234567890);
        j.put("scenicId", 1);
        j.put("name", "中文测试");
        j.put("stars", 4);

        MyMeilisearch.getInstance().addOne("scenicspot", JsonArray.of(j))
                .onSuccess(msg::reply);
    }

    @Path("/testMeiliSearch")
    public void testMeiliSearch(Message<JsonObject> msg) {
        SearchRequest sr = new SearchRequest();
        sr.setQ("*");

        MyMeilisearch.getInstance().search("scenicspot", sr)
                .onSuccess(msg::reply);
    }

    @Path("/testMeiliDelete")
    public void testMeiliDelete(Message<JsonObject> msg) {
        MyMeilisearch.getInstance().deleteOne("scenicspot", "1234567890")
                .onSuccess(msg::reply);
    }

    @Path("/testGetOne")
    public void testGetOne(Message<JsonObject> msg) {
        MyMeilisearch.getInstance().getOne("scenicspot", "1234567890")
                .onSuccess(msg::reply);
    }

    @Path("/testmultiSearch")
    public void testmultiSearch(Message<JsonObject> msg) {
        MultiSearchRequest request = new MultiSearchRequest();
        MultiSearchQuery query1 = new MultiSearchQuery();
        query1.setIndexUid("scenicspot");
        query1.setQ("*");
        MultiSearchQuery query2 = new MultiSearchQuery();
        query2.setIndexUid("scenicspot");
        query2.setQ("中文测试");
        List<MultiSearchQuery> queries = List.of(query1, query2);
        request.setQueries(queries);
        MyMeilisearch.getInstance().multiSearch(request)
                .onSuccess(msg::reply);
    }

    @Path("/testmultiSearchFederate")
    public void testmultiSearchFederate(Message<JsonObject> msg) {
        MultiSearchRequest request = new MultiSearchRequest();
        MultiSearchQuery query1 = new MultiSearchQuery();
        query1.setIndexUid("scenicspot");
        query1.setQ("*");
        MultiSearchQuery query2 = new MultiSearchQuery();
        query2.setIndexUid("scenicspot");
        query2.setQ("中文测试");
        List<MultiSearchQuery> queries = List.of(query1, query2);
        request.setQueries(queries);
        MultiSearchFederation federate = new MultiSearchFederation();
        federate.setLimit(10);
        request.setFederation(federate);
        MyMeilisearch.getInstance().multiSearchFederate(request)
                .onSuccess(msg::reply);
    }

}
