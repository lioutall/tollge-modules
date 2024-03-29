package com.tollge.modules.microservices.eureka;

import com.tollge.common.util.MyVertx;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EurekaClientUtil {
    public static <T> void get(String vipAddress, String uri, JsonObject params, Handler<AsyncResult<Message<T>>> replyHandler) {
        MyVertx.vertx().eventBus()
               .request("eureka:get", new JsonObject().put("vipAddress", vipAddress)
                                                 .put("requestURI", uri)
                                                 .put("params", params), replyHandler);
    }

    public static <T> void post(String vipAddress, String uri, JsonObject params, JsonObject body, Handler<AsyncResult<Message<T>>> replyHandler) {
        MyVertx.vertx().eventBus()
               .request("eureka:post", new JsonObject().put("vipAddress", vipAddress)
                                                   .put("requestURI", uri)
                                                   .put("params", params)
                                                   .put("body", body), replyHandler);
    }
}
