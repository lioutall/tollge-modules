package com.tollge.modules.data.meilisearch;

import com.tollge.common.TollgeException;
import com.tollge.common.util.MyVertx;
import com.tollge.common.util.Properties;
import com.tollge.modules.data.meilisearch.pojo.*;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyMeilisearch {

    /*****************************
     * https://www.meilisearch.com/docs/reference/api/documents
     **********/

    private WebClient client;
    private String host;
    private String masterKey;
    private Integer port;
    private Boolean isSsl;

    private MyMeilisearch() {
        client = WebClient.create(MyVertx.vertx());
        isSsl = Properties.getBoolean("meilisearch", "isSsl");
        if (isSsl == null) {
            isSsl = false;
        }
        port = Properties.getInteger("meilisearch", "port");
        if (port == null) {
            port = isSsl ? 443 : 80;
        }
        host = Properties.getString("meilisearch", "host");
        masterKey = Properties.getString("meilisearch", "masterKey");

        if(host == null || masterKey == null) {
            log.error("meilisearch host or masterKey is null");
        }
    }

    private enum Singleton {
        // 单例
        INSTANCE;

        private MyMeilisearch single;

        private Singleton() {
            single = new MyMeilisearch();
        }

        public MyMeilisearch getInstance() {
            return single;
        }
    }

    public static MyMeilisearch getInstance() {
        return Singleton.INSTANCE.getInstance();
    }



    /**
     * Add or replace documents
     */
    public Future<JsonObject> addOne(String index, JsonArray documents) {
        log.debug("addOne index:{}, documents:{}", index, documents);
        return Future.future(reply ->
                client.post(port, host, "/indexes/" + index + "/documents")
                        .ssl(isSsl)
                        .putHeader("Content-Type", "application/json")
                        .putHeader("Authorization", "Bearer " + masterKey)
                        .sendJson(documents, ar -> {
                             if (ar.succeeded()) {
                                 Buffer body = ar.result().body();
                                 log.debug("addOne response: " + body);
                                 if(ar.result().statusCode() != 200 && ar.result().statusCode() != 202) {
                                     log.error("addOne error: " + body);
                                     reply.handle(Future.failedFuture(new TollgeException(body.toString())));
                                 } else {
                                     reply.handle(Future.succeededFuture(ar.result().bodyAsJsonObject()));
                                 }
                             } else {
                                 log.error("addOne error: ", ar.cause());
                                 reply.handle(Future.failedFuture(ar.cause()));
                             }
                         })
        );
    }

    /**
     * Delete one document
     */
    public Future<JsonObject> deleteOne(String index, String documentId) {
        log.debug("deleteOne index:{}, documentId:{}", index, documentId);
        return Future.future(reply ->
                client.delete(port, host, "/indexes/" + index + "/documents/" + documentId)
                        .ssl(isSsl)
                        .putHeader("Authorization", "Bearer " + masterKey)
                        .send(ar -> {
                             if (ar.succeeded()) {
                                 Buffer body = ar.result().body();
                                 log.debug("deleteOne response: " + body);
                                 if(ar.result().statusCode() != 200 && ar.result().statusCode() != 202) {
                                     log.error("deleteOne error: " + body);
                                     reply.handle(Future.failedFuture(new TollgeException(body.toString())));
                                 } else {
                                     reply.handle(Future.succeededFuture(ar.result().bodyAsJsonObject()));
                                 }
                             } else {
                                 log.error("deleteOne error: ", ar.cause());
                                 reply.handle(Future.failedFuture(ar.cause()));
                             }
                         })
        );
    }

    /**
     * Delete documents by batch
     */
    public Future<JsonObject> deleteMany(String index, JsonArray documentIds) {
        log.debug("deleteMany index:{}, documentIds:{}", index, documentIds);
        return Future.future(reply ->
                client.post(port, host,"/indexes/" + index + "/documents/delete-batch")
                        .ssl(isSsl)
                        .putHeader("Content-Type", "application/json")
                        .putHeader("Authorization", "Bearer " + masterKey)
                        .sendJson(documentIds, ar -> {
                            if (ar.succeeded()) {
                                Buffer body = ar.result().body();
                                log.debug("deleteMany response: " + body);
                                if (ar.result().statusCode() != 200 && ar.result().statusCode() != 202) {
                                    log.error("deleteMany error: " + body);
                                    reply.handle(Future.failedFuture(new TollgeException(body.toString())));
                                } else {
                                    reply.handle(Future.succeededFuture(ar.result().bodyAsJsonObject()));
                                }
                            } else {
                                log.error("deleteMany error: ", ar.cause());
                                reply.handle(Future.failedFuture(ar.cause()));
                            }
                         })
        );
    }

    /**
     * Get one document
     */
    public Future<JsonObject> getOne(String index, String documentId) {
        log.debug("getOne index:{}, documentId:{}", index, documentId);
        return Future.future(reply ->
                client.get(port, host,"/indexes/" + index + "/documents/" + documentId)
                        .ssl(isSsl)
                        .putHeader("Authorization", "Bearer " + masterKey)
                        .send(ar -> {
                             if (ar.succeeded()) {
                                 Buffer body = ar.result().body();
                                 log.debug("getOne response: " + body);
                                 if(ar.result().statusCode() != 200 && ar.result().statusCode() != 202) {
                                     log.error("getOne error: " + body);
                                     reply.handle(Future.failedFuture(new TollgeException(body.toString())));
                                 } else {
                                     reply.handle(Future.succeededFuture(ar.result().bodyAsJsonObject()));
                                 }
                             } else {
                                 log.error("getOne error: ", ar.cause());
                                 reply.handle(Future.failedFuture(ar.cause()));
                             }
                         })
        );
    }

    /**
     * Search in an index with POST
     */
    public Future<SearchResponse> search(String index, SearchRequest request) {
        log.debug("search index:{}, request:{}", index, request);
        return Future.future(reply ->
                client.post(port, host, "/indexes/" + index + "/search")
                        .ssl(isSsl)
                        .putHeader("Content-Type", "application/json")
                        .putHeader("Authorization", "Bearer " + masterKey)
                        .sendJson(request, ar -> {
                            if (ar.succeeded()) {
                                Buffer body = ar.result().body();
                                log.debug("search response: " + body);
                                if(ar.result().statusCode() != 200 && ar.result().statusCode() != 202) {
                                    log.error("search error: " + body);
                                    reply.handle(Future.failedFuture(new TollgeException(body.toString())));
                                } else {
                                    reply.handle(Future.succeededFuture(Json.decodeValue(ar.result().body(), SearchResponse.class)));
                                }
                            } else {
                                log.error("search error: ", ar.cause());
                                reply.handle(Future.failedFuture(ar.cause()));
                            }
                        })
        );
    }

    /**
     * Perform a multi-search
     * Non-federated multi-search
     */
    public Future<MultiSearchResponse> multiSearch(MultiSearchRequest request) {
        log.debug("multiSearch request:{}", request);
        request.setFederation(null);
        return Future.future(reply ->
                client.post(port, host, "/multi-search")
                        .ssl(isSsl)
                        .putHeader("Content-Type", "application/json")
                        .putHeader("Authorization", "Bearer " + masterKey)
                        .sendJson(request, ar -> {
                             if (ar.succeeded()) {
                                 Buffer body = ar.result().body();
                                 log.debug("multiSearch response: " + body);
                                 if(ar.result().statusCode() != 200 && ar.result().statusCode() != 202) {
                                     log.error("multiSearch error: " + body);
                                     reply.handle(Future.failedFuture(new TollgeException(body.toString())));
                                 } else {
                                     reply.handle(Future.succeededFuture(Json.decodeValue(ar.result().body(), MultiSearchResponse.class)));
                                 }
                             } else {
                                 log.error("multiSearch error: ", ar.cause());
                                 reply.handle(Future.failedFuture(ar.cause()));
                             }
                         })
        );
    }

    /**
     * Perform a multi-search
     * federated multi-search
     */
    public Future<MultiSearchFederateResponse> multiSearchFederate(MultiSearchRequest request) {
        log.debug("multiSearchFederate request:{}", request);
        return Future.future(reply ->
                client.post(port, host, "/multi-search")
                        .ssl(isSsl)
                        .putHeader("Content-Type", "application/json")
                        .putHeader("Authorization", "Bearer " + masterKey)
                        .sendJson(request, ar -> {
                            if (ar.succeeded()) {
                                Buffer body = ar.result().body();
                                log.debug("multiSearchFederate response: " + body);
                                if(ar.result().statusCode() != 200 && ar.result().statusCode() != 202) {
                                    log.error("multiSearchFederate error: " + body);
                                    reply.handle(Future.failedFuture(new TollgeException(body.toString())));
                                } else {
                                    reply.handle(Future.succeededFuture(Json.decodeValue(body, MultiSearchFederateResponse.class)));
                                }
                            } else {
                                log.error("multiSearchFederate error: ", ar.cause());
                                reply.handle(Future.failedFuture(ar.cause()));
                            }
                        })
        );
    }





}