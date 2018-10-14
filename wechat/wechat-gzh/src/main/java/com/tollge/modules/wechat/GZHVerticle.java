package com.tollge.modules.wechat;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.RefreshPolicy;
import com.alicp.jetcache.embedded.CaffeineCacheBuilder;
import com.google.common.base.Preconditions;
import com.tollge.common.annotation.mark.Biz;
import com.tollge.common.annotation.mark.Path;
import com.tollge.common.annotation.valid.NotNull;
import com.tollge.common.verticle.BizVerticle;
import io.netty.util.internal.StringUtil;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static com.tollge.modules.wechat.GZHUtil.APPID;
import static com.tollge.modules.wechat.GZHUtil.SECRET;

/**
 * 微信公众号
 */
@Slf4j
@Biz(value = "third://gzh", instances = 1)
public class GZHVerticle extends BizVerticle {

    private static final int EXPIRE_BEFORE = 7200;
    private static final String API_WEIXIN_QQ_COM = "api.weixin.qq.com";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String OPENID = "openid";

    // 120分钟自动刷新, 30分钟未访问则停止
    private static RefreshPolicy policy = RefreshPolicy.newPolicy(EXPIRE_BEFORE - 30L, TimeUnit.SECONDS)
            .stopRefreshAfterLastAccess(30, TimeUnit.MINUTES);

    // 内存缓存
    private static Cache<String, String> cache = CaffeineCacheBuilder.createCaffeineCacheBuilder()
            .expireAfterWrite(EXPIRE_BEFORE, TimeUnit.SECONDS)
            .loader(key -> {
                log.debug("begin fetch gzh token!");

                URL obj = new URL("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + APPID + "&secret=" + SECRET);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

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
            })
            .refreshPolicy(policy)
            .buildCache();

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

        client.get(443, API_WEIXIN_QQ_COM, "/sns/oauth2/access_token?appid=" + APPID
                + "&secret=" + SECRET + "&code=" + jo.getString("code") + "&grant_type=authorization_code")
                .ssl(true)
                .send(res -> {
                    if (res.succeeded()) {
                        JsonObject jsonObject = res.result().bodyAsJsonObject();
                        jsonObject.put(OPENID, jsonObject.getString(OPENID));
                        msg.reply(jsonObject);
                    } else {
                        log.error("code2accessCode failed", res.cause());
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

        client.get(443, API_WEIXIN_QQ_COM, "/sns/userinfo?access_token=" + jo.getString(ACCESS_TOKEN)
                + "&openid=" + jo.getString(OPENID) + "&lang=zh_CN")
                .ssl(true)
                .send(res -> {
                    if (res.succeeded()) {
                        JsonObject jsonObject = res.result().bodyAsJsonObject();
                        msg.reply(jsonObject);
                    } else {
                        log.error("userInfo failed", res.cause());
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
        log.info("[临时二维码] start,msg:{}", msg.body());
        JsonObject body = new JsonObject("{\"action_info\": {\"scene\": {\"scene_str\": \"" + msg.body().getString("sceneStr") + "\"}}}")
                .put("expire_seconds", msg.body().getInteger("expireSeconds"))
                .put("action_name", "QR_STR_SCENE");

        WebClient client = WebClient.create(vertx);

        client.post(443, API_WEIXIN_QQ_COM, "/cgi-bin/qrcode/create?access_token=" + cache.get(""))
                .ssl(true)
                .sendJsonObject(body,
                        res -> {
                            if (res.succeeded()) {
                                log.info("[临时二维码] end, msg:{}", res.result().bodyAsString());
                                msg.reply(res.result().bodyAsJsonObject());
                            } else {
                                log.error("[临时二维码] failed", res.cause());
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
        log.info("[链接转短链] start,msg:{}", msg.body());
        JsonObject body = new JsonObject("{\"action\": \"long2short\"}")
                .put("long_url", msg.body().getString("url"));

        WebClient client = WebClient.create(vertx);

        client.post(443, API_WEIXIN_QQ_COM, "/cgi-bin/shorturl?access_token=" + cache.get(""))
                .ssl(true)
                .sendJsonObject(body,
                        res -> {
                            if (res.succeeded()) {
                                log.info("[链接转短链] end, msg:{}", res.result().bodyAsString());
                                msg.reply(res.result().bodyAsJsonObject());
                            } else {
                                log.error("[链接转短链] failed", res.cause());
                            }
                        });
    }

}
