package com.tollge.modules.wechat;

import com.google.common.base.Preconditions;
import com.tollge.common.StatusCodeMsg;
import com.tollge.common.annotation.mark.Biz;
import com.tollge.common.annotation.mark.Path;
import com.tollge.common.annotation.valid.NotNull;
import com.tollge.common.util.Properties;
import com.tollge.common.verticle.BizVerticle;
import com.tollge.modules.data.redis.MyRedis;
import io.netty.util.internal.StringUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static com.tollge.modules.wechat.GZHUtil.APPID;
import static com.tollge.modules.wechat.GZHUtil.SECRET;

/**
 * 微信公众号
 */
@Slf4j
@Biz(value = "third://gzh", instances = 1)
public class GZHVerticle extends BizVerticle {

    private static final int EXPIRE_BEFORE = 7200;
    private static final String API_WEIXIN_QQ_COM = Properties.getString("wechat", "gzh.host");
    private static final boolean API_WEIXIN_QQ_COM_SSL = Properties.getBoolean("wechat", "gzh.isSSL");
    private static final int API_WEIXIN_QQ_COM_PORT = Properties.getInteger("wechat", "gzh.port");
    private static final String ACCESS_TOKEN = "access_token";
    private static final String OPENID = "openid";

    private static final String KEY_OF_GZH_TOKEN = "tollge:key-gzh";
    private static final String LOCK_OF_GZH_TOKEN = "tollge:lock-gzh-token";
    private static final String LOCK_OF_GZH_JSTICKET = "tollge:lock-gzh-jsticket";

    static Random random = new Random();

    private LocalDateTime refreshTime = LocalDateTime.now();

    // 内存缓存
    private static String fetchGzhToken() {
        try {
            log.debug("begin fetch gzh token!");

            String port = API_WEIXIN_QQ_COM_SSL && API_WEIXIN_QQ_COM_PORT == 443 ? "" : ":" + API_WEIXIN_QQ_COM_PORT;
            String url = (API_WEIXIN_QQ_COM_SSL ? "https" : "http") + "://" + API_WEIXIN_QQ_COM + port + "/cgi-bin/token?grant_type=client_credential&appid=" + APPID + "&secret=" + SECRET;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setConnectTimeout(5000);

            con.setRequestMethod("GET");
            StringBuilder response = new StringBuilder();

            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
            log.debug("end fetch gzh token:{}", response);

            return new JsonObject(response.toString()).getString(ACCESS_TOKEN);
        } catch (IOException e) {
            log.error("fetchGzhToken error", e);
        }
        return null;
    }

    private static void fetchJsTicket(Handler<AsyncResult<String>> onGet) {

        getToken(r -> {
            try {
                if (r.succeeded() && r.result() != null) {
                    log.debug("begin fetch js ticket!");

                    String port = API_WEIXIN_QQ_COM_SSL && API_WEIXIN_QQ_COM_PORT == 443 ? "" : ":" + API_WEIXIN_QQ_COM_PORT;
                    String url = (API_WEIXIN_QQ_COM_SSL ? "https" : "http") + "://" + API_WEIXIN_QQ_COM + port + "/cgi-bin/ticket/getticket?access_token=" + r.result().toString() + "&type=jsapi";
                    URL obj = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    con.setConnectTimeout(5000);

                    con.setRequestMethod("GET");
                    StringBuilder response = new StringBuilder();

                    try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                        String inputLine;

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                    }
                    log.debug("end fetch js ticket:{}", response);

                    onGet.handle(Future.succeededFuture(new JsonObject(response.toString()).getString("ticket")));
                } else {
                    onGet.handle(Future.failedFuture("fetchGzhToken null"));
                }
            } catch (IOException e) {
                log.error("fetchGzhToken error", e);
                onGet.handle(Future.failedFuture("fetchGzhToken error"));
            }
        });
    }

    public static void getToken(Handler<AsyncResult<String>> onGet) {
        MyRedis.get(KEY_OF_GZH_TOKEN).onSuccess(r -> {
            if (r == null) {
                // 重新获取并返回
                MyRedis.tryGetDistributedLock(LOCK_OF_GZH_TOKEN).onComplete(lock -> {
                    if (lock.succeeded() && lock.result() != null) {
                        String s = fetchGzhToken();
                        if (s != null) {
                            MyRedis.set(KEY_OF_GZH_TOKEN, s, EXPIRE_BEFORE * 1000);
                        }
                        onGet.handle(Future.succeededFuture(s));
                        MyRedis.releaseDistributedLock(LOCK_OF_GZH_TOKEN);
                    } else {
                        log.error("get lock error", lock.cause());
                    }
                });
            } else {
                // 直接返回
                onGet.handle(Future.succeededFuture(r.toString()));

                // 获取ttl
                MyRedis.ttl(KEY_OF_GZH_TOKEN).onComplete(ttl -> {
                    if (ttl.succeeded() && ttl.result() != null) {
                        long ttlSeconds = ttl.result().toLong();
                        if (ttlSeconds < 30 * 60) {
                            MyRedis.tryGetDistributedLock(LOCK_OF_GZH_TOKEN).onComplete(lock -> {
                                if (lock.succeeded() && lock.result() != null) {
                                    String s = fetchGzhToken();
                                    if (s != null) {
                                        MyRedis.set(KEY_OF_GZH_TOKEN, s, EXPIRE_BEFORE * 1000);
                                    }
                                    MyRedis.releaseDistributedLock(LOCK_OF_GZH_TOKEN);
                                }
                            });
                        }
                    }
                });
            }
        }).onFailure(e ->
        {
            log.error("get gzh token error", e);
            onGet.handle(Future.failedFuture("get gzh token error"));
        });
    }

    /**
     * 通过code换取网页授权网页access_token
     *
     * @param msg []
     */
    @Path("/code2accessCode")
    @NotNull(key = "code", msg = "code is required")
    public void code2accessCode(Message<JsonObject> msg) {
        JsonObject jo = msg.body();
        WebClient client = WebClient.create(vertx);

        client.get(API_WEIXIN_QQ_COM_PORT, API_WEIXIN_QQ_COM, "/sns/oauth2/access_token?appid=" + APPID
                        + "&secret=" + SECRET + "&code=" + jo.getString("code") + "&grant_type=authorization_code")
                .ssl(API_WEIXIN_QQ_COM_SSL)
                .send(res -> {
                    if (res.succeeded()) {
                        JsonObject jsonObject = res.result().bodyAsJsonObject();
                        jsonObject.put(OPENID, jsonObject.getString(OPENID));
                        msg.reply(jsonObject);
                    } else {
                        log.error("code2accessCode failed", res.cause());
                        msg.fail(StatusCodeMsg.C404.getCode(), res.cause().getMessage());
                    }
                });
    }

    /**
     * 拉取用户信息(需scope为 snsapi_userinfo)
     *
     * @param msg []
     */
    @Path("/userInfo")
    public void userInfo(Message<JsonObject> msg) {
        JsonObject jo = msg.body();
        Preconditions.checkArgument(jo != null, "msg should not be null.");
        Preconditions.checkArgument(!StringUtil.isNullOrEmpty(jo.getString(ACCESS_TOKEN)), "access_token should not be null.");
        Preconditions.checkArgument(!StringUtil.isNullOrEmpty(jo.getString(OPENID)), "openid should not be null.");
        WebClient client = WebClient.create(vertx);

        client.get(API_WEIXIN_QQ_COM_PORT, API_WEIXIN_QQ_COM, "/sns/userinfo?access_token=" + jo.getString(ACCESS_TOKEN)
                        + "&openid=" + jo.getString(OPENID) + "&lang=zh_CN")
                .ssl(API_WEIXIN_QQ_COM_SSL)
                .send(res -> {
                    if (res.succeeded()) {
                        JsonObject jsonObject = res.result().bodyAsJsonObject();
                        msg.reply(jsonObject);
                    } else {
                        log.error("userInfo failed", res.cause());
                        msg.fail(StatusCodeMsg.C404.getCode(), res.cause().getMessage());
                    }
                });
    }

    /**
     * 临时二维码
     *
     * @param msg []
     */
    @Path("/qrSCENE")
    @NotNull(key = "expireSeconds")
    @NotNull(key = "sceneStr")
    public void qrSCENE(Message<JsonObject> msg) {
        getToken(r -> {
            if (r.succeeded() && r.result() != null) {
                log.info("[临时二维码] start,msg:{}", msg.body());
                JsonObject body = new JsonObject("{\"action_info\": {\"scene\": {\"scene_str\": \"" + msg.body().getString("sceneStr") + "\"}}}")
                        .put("expire_seconds", msg.body().getInteger("expireSeconds"))
                        .put("action_name", "QR_STR_SCENE");

                WebClient client = WebClient.create(vertx);

                client.post(API_WEIXIN_QQ_COM_PORT, API_WEIXIN_QQ_COM, "/cgi-bin/qrcode/create?access_token=" + r.result())
                        .ssl(API_WEIXIN_QQ_COM_SSL)
                        .sendJsonObject(body,
                                res -> {
                                    if (res.succeeded()) {
                                        log.info("[临时二维码] end, msg:{}", res.result().bodyAsString());
                                        msg.reply(res.result().bodyAsJsonObject());
                                    } else {
                                        log.error("[临时二维码] failed", res.cause());
                                        msg.fail(StatusCodeMsg.C404.getCode(), res.cause().getMessage());
                                    }
                                });
            } else {
                log.error("fetch token error");
                msg.fail(StatusCodeMsg.C404.getCode(), "fetch token error");
            }
        });
    }

    /**
     * 长链接转短链接接口
     *
     * @param msg []
     */
    @Path("/long2short")
    @NotNull(key = "url")
    public void long2short(Message<JsonObject> msg) {
        getToken(r -> {
            if (r.succeeded() && r.result() != null) {
                log.info("[链接转短链] start,msg:{}", msg.body());
                JsonObject body = new JsonObject("{\"action\": \"long2short\"}")
                        .put("long_url", msg.body().getString("url"));

                WebClient client = WebClient.create(vertx);

                client.post(API_WEIXIN_QQ_COM_PORT, API_WEIXIN_QQ_COM, "/cgi-bin/shorturl?access_token=" + r.result())
                        .ssl(API_WEIXIN_QQ_COM_SSL)
                        .sendJsonObject(body,
                                res -> {
                                    if (res.succeeded()) {
                                        log.info("[链接转短链] end, msg:{}", res.result().bodyAsString());
                                        msg.reply(res.result().bodyAsJsonObject());
                                    } else {
                                        log.error("[链接转短链] failed", res.cause());
                                        msg.fail(StatusCodeMsg.C404.getCode(), res.cause().getMessage());
                                    }
                                });
            } else {
                log.error("fetch token error");
                msg.fail(StatusCodeMsg.C404.getCode(), "fetch token error");
            }
        });
    }

    /**
     * 通过config接口注入权限验证配置
     *
     * @param msg []
     */
    @Path("/wxConfig")
    public void jsTicket(Message<JsonObject> msg) {

        fetchJsTicket(r -> {
            if (r.succeeded() && r.result() != null) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.put("jsapi_ticket", r.result());
                jsonObject.put("noncestr", random.nextLong());
                jsonObject.put("timestamp", System.currentTimeMillis() / 1000);
                jsonObject.put("url", msg.body().getString("url"));

                // 对所有待签名参数按照字段名的ASCII 码从小到大排序（字典序）后，使用URL键值对的格式（即key1=value1&key2=value2…）拼接成字符串string1
                String string1 = jsonObject.stream().sorted(Comparator.comparing(Map.Entry::getKey))
                        .map(o -> o.getKey() + "=" + o.getValue()).collect(Collectors.joining("&"));

                try {
                    JsonObject result = new JsonObject();
                    result.put("timestamp", jsonObject.getValue("timestamp"));
                    result.put("nonceStr", jsonObject.getValue("noncestr").toString());
                    result.put("signature", SHA1.encode(string1));
                    result.put("appId", APPID);
                    msg.reply(result);
                } catch (NoSuchAlgorithmException e) {
                    log.error("sha1 失败", e);
                    msg.fail(StatusCodeMsg.C201.getCode(), "sha1 失败");
                }
            } else {
                log.error("fetch token error");
                msg.fail(StatusCodeMsg.C404.getCode(), "fetch token error");
            }
        });


    }

}
