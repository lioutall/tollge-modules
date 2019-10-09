package com.tollge.modules.microservices.eureka.client;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.MyDataCenterInstanceConfig;
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClient;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * eureka服务(Provider)Verticle
 *
 * @author toyer
 */
@Slf4j
public class EurekaHttpClientVerticle extends AbstractVerticle {
    WebClient webClient;

    private EurekaClient eurekaClient;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        webClient = WebClient.create(vertx);

        EurekaInstanceConfig instanceConfig = new MyDataCenterInstanceConfig();
        InstanceInfo instanceInfo = new EurekaConfigBasedInstanceInfoProvider(instanceConfig).get();
        ApplicationInfoManager applicationInfoManager = new ApplicationInfoManager(instanceConfig, instanceInfo);

        eurekaClient = new DiscoveryClient(applicationInfoManager, new DefaultEurekaClientConfig());
    }

    @Override
    public void start() {
        vertx.eventBus().consumer("eureka:get", this::get);
        vertx.eventBus().consumer("eureka:post", this::post);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        vertx.executeBlocking(fut->{
            if (eurekaClient != null) {
                log.info("Shutting down EurekaHttpClientVerticle...");
                eurekaClient.shutdown();
            }
        }, res->{
            if(res.failed()) {
                log.error("Shutting down EurekaHttpClientVerticle error", res.cause());
            }
        });
    }

    protected void get(Message<JsonObject> msg) {
        InstanceInfo nextServerInfo = null;
        JsonObject body = msg.body();
        try {
            nextServerInfo = eurekaClient.getNextServerFromEureka(body.getString("vipAddress"), body.getBoolean("secure", false));
            String ip = nextServerInfo.getIPAddr();
            int port = nextServerInfo.getPort();

            HttpRequest<Buffer> httpRequest = webClient.get(port, ip, body.getString("requestURI")).timeout(3000);
            JsonObject params = body.getJsonObject("params");
            if (params != null && !params.isEmpty()) {
                for (Map.Entry<String, Object> param : params) {
                    httpRequest.addQueryParam(param.getKey(), (String) param.getValue());
                }
            }
            httpRequest.as(BodyCodec.jsonObject()).send(ar -> {
                if (ar.succeeded()) {
                    HttpResponse<JsonObject> response = ar.result();
                    msg.reply(response.body());
                } else {
                    log.error("EurekaHttpClientVerticle.get wrong={} ",ar.cause().getMessage(), ar.cause());
                    msg.fail(501, ar.cause().getMessage());
                }
            });
        } catch (Exception e) {
            log.error("getNextServerFromEureka failed, vipAddress={}", body.getString("vipAddress"), e);
            msg.fail(501, e.getMessage());
        }
    }

    protected void post(Message<JsonObject> msg) {
        InstanceInfo nextServerInfo = null;
        JsonObject body = msg.body();
        try {
            nextServerInfo = eurekaClient.getNextServerFromEureka(body.getString("vipAddress"), body.getBoolean("secure", false));
            String ip = nextServerInfo.getIPAddr();
            int port = nextServerInfo.getPort();

            HttpRequest<Buffer> httpRequest = webClient.post(port, ip, body.getString("requestURI")).timeout(3000);

            JsonObject params = body.getJsonObject("params");
            if (params != null && !params.isEmpty()) {
                for (Map.Entry<String, Object> param : params) {
                    httpRequest.addQueryParam(param.getKey(), (String) param.getValue());
                }
            }

            httpRequest.as(BodyCodec.jsonObject()).sendJson(body.getValue("body"), ar -> {
                if (ar.succeeded()) {
                    HttpResponse<JsonObject> response = ar.result();
                    msg.reply(response.body());
                } else {
                    log.error("EurekaHttpClientVerticle.post wrong={} ",ar.cause().getMessage(), ar.cause());
                    msg.fail(501, ar.cause().getMessage());
                }
            });
        } catch (Exception e) {
            log.error("getNextServerFromEureka failed, vipAddress={}", body.getString("vipAddress"), e);
            msg.fail(501, e.getMessage());
        }
    }

}
