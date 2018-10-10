package com.tollge.modules.wechat;

import com.google.common.base.Preconditions;
import io.netty.util.internal.StringUtil;
import  com.tollge.common.annotation.Method;
import  com.tollge.common.annotation.mark.Path;
import  com.tollge.common.annotation.mark.Router;
import  com.tollge.common.util.Properties;
import  com.tollge.common.verticle.AbstractRouter;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor
@Router("/gzh")
@Slf4j
public class GZHRouter extends AbstractRouter {

    private static final String EVENT_TOKEN = Properties.getString("wechat", "event.token");
    private static final String EVENT_SUBSCRIBE = "subscribe";
    private static final String EVENT_SCAN = "SCAN";


    final static String WEB_URL = Properties.getString("wechat", "web.url");

    /**
     * 监听事件消息
     *
     * @param rct []
     */
    @Path(value = "/event")
    public void list(RoutingContext rct) {
        MultiMap params = rct.queryParams();
        String body = rct.getBodyAsString();
        log.info("监听到微信事件:\nbody:{}\nquery:{}", body, params);
        String echostr = params.get("echostr");
        String timestamp = params.get("timestamp");
        String nonce = params.get("nonce");
        String signature = params.get("signature");
        Preconditions.checkArgument(!StringUtil.isNullOrEmpty(timestamp), "参数错误");
        Preconditions.checkArgument(!StringUtil.isNullOrEmpty(nonce), "参数错误");
        Preconditions.checkArgument(!StringUtil.isNullOrEmpty(signature), "参数错误");

        String str = Stream.of(EVENT_TOKEN, timestamp, nonce).sorted().collect(Collectors.joining());


        try {
            if (!signature.equalsIgnoreCase(SHA1Util.encode(str))) {
                throw new IllegalArgumentException();
            }
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            log.error("sha1 失败", e);
            rct.response().end();
            return;
        }

        // 拿eventKey
        if (body != null && body.contains("<")) {
            String eventKey = getValue(body, "EventKey");
            String event = getValue(body, "Event");
            String fromUserName = getValue(body, "FromUserName");

            Preconditions.checkArgument(!StringUtil.isNullOrEmpty(event), "参数错误");
            Preconditions.checkArgument(!StringUtil.isNullOrEmpty(fromUserName), "参数错误");

            switch (event) {
                case EVENT_SCAN:
                    Preconditions.checkArgument(!StringUtil.isNullOrEmpty(eventKey), "参数错误");
                    rct.vertx().eventBus().send("biz://gzh/scan", new JsonObject().put("openId", fromUserName).put("key", eventKey));
                    break;
                case EVENT_SUBSCRIBE:
                    Preconditions.checkArgument(!StringUtil.isNullOrEmpty(eventKey), "参数错误");
                    rct.vertx().eventBus().send("biz://gzh/scan", new JsonObject().put("openId", fromUserName).put("key", eventKey.replace("qrscene_", "")));
                    break;
                default:
                    break;
            }
        }

        rct.response().end(echostr == null ? "" : echostr);
    }

    /**
     * 跳转
     */
    @Path(value = "/redirect", method = Method.GET)
    public void redirect(RoutingContext rct) {
        // 获取是否微信
        String userAgent = rct.request().getHeader("user-agent");
        // 微信过来的请求
        if (userAgent != null && userAgent.toLowerCase().contains("micromessenger")) {
            // 获取框码
            String code = rct.queryParams().get("code");
            String uri = rct.queryParams().get("uri");
            String url = "";
            try {
                url = GZHVerticle.redirectUrl(URLEncoder.encode(WEB_URL + uri, "UTF-8"), code);
            } catch (UnsupportedEncodingException e) {
                log.error("URLEncoder.encode失败", e);
            }

            // 跳转到微信登录
            rct.response()
                    .putHeader("location", url)
                    .setStatusCode(302).end();
        } else {
            rct.response().end();
        }
    }

    /**
     * 解析微信XML-定制
     */
    private static String getValue(String source, String key) {
        int beginIdx = source.indexOf("<" + key);
        int endIdx = source.indexOf("</" + key);

        String value = source.substring(beginIdx + key.length() + 2, endIdx);
        if (value.startsWith("<![CDATA[")) {
            return value.substring(9, value.length() - 3);
        }
        return value;
    }
}
