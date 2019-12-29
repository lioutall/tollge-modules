package com.tollge.modules.wechat;

import com.google.common.base.Preconditions;
import com.tollge.common.annotation.Method;
import com.tollge.common.annotation.mark.Path;
import com.tollge.common.util.Properties;
import com.tollge.common.verticle.AbstractRouter;
import com.tollge.modules.web.http.Http;
import io.netty.util.internal.StringUtil;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.tollge.common.simple.Handle.assertSuccess;

@NoArgsConstructor
@Http("/gzh")
@Slf4j
public class GZHRouter extends AbstractRouter {

    private static final String EVENT_TOKEN = Properties.getString("wechat", "event.token");
    private static final String EVENT_SUBSCRIBE = "subscribe";
    private static final String EVENT_SCAN = "SCAN";
    private static final String TEXT = "text";
    private static final String EVENT = "event";

    /**
     * 监听事件消息
     *
     * @param rct []
     */
    @Path(value = "/event", contentType = "text/xml")
    public void event(RoutingContext rct) {
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
            if (!signature.equals(SHA1.encode(str))) {
                throw new IllegalArgumentException();
            }
        } catch (NoSuchAlgorithmException e) {
            log.error("sha1 失败", e);
            rct.response().end();
            return;
        }

        if(!StringUtil.isNullOrEmpty(echostr)) {
            rct.response().end(echostr);
        } else {
            if (body != null && body.contains("<")) {
                // 确定消息类型
                String msgType = getValue(body, "MsgType");
                Preconditions.checkArgument(!StringUtil.isNullOrEmpty(msgType), "参数[MsgType]错误");
                String fromUserName = getValue(body, "FromUserName");
                Preconditions.checkArgument(!StringUtil.isNullOrEmpty(fromUserName), "参数[FromUserName]错误");

                switch (msgType) {
                    case EVENT:
                        String event = getValue(body, "Event");
                        Preconditions.checkArgument(!StringUtil.isNullOrEmpty(event), "参数[Event]错误");

                        if(EVENT_SCAN.equalsIgnoreCase(event)) {
                            String eventKey = getValue(body, "EventKey");
                            Preconditions.checkArgument(!StringUtil.isNullOrEmpty(eventKey), "参数[EventKey]错误");
                            rct.vertx().eventBus().<String>request("biz://gzh/event/scan", new JsonObject().put("openId", fromUserName).put("key", eventKey),
                                    assertSuccess(rct, responseGZH(rct)));
                        } else if(EVENT_SUBSCRIBE.equalsIgnoreCase(event)) {
                            String eventkey = getValue(body, "EventKey");
                            if (StringUtil.isNullOrEmpty(eventkey)) {
                                rct.response().end("");
                            } else {
                                rct.vertx().eventBus().<String>request("biz://gzh/event/scan", new JsonObject().put("openId", fromUserName).put("key", eventkey.replace("qrscene_", "")),
                                        assertSuccess(rct, responseGZH(rct)));
                            }
                        } else {
                            rct.response().end("");
                        }
                        break;
                    case TEXT:
                        rct.vertx().eventBus().<String>request("biz://gzh/event/text", new JsonObject().put("openId", fromUserName).put("text", getValue(body, "Content")),
                                assertSuccess(rct, responseGZH(rct)));
                        break;
                    default:
                        rct.response().end("");
                        break;
                }
            }
        }

    }

    private Handler<Message<String>> responseGZH(RoutingContext rct) {
        return res -> {
            String r = res.body();
            log.info("gzh/event 返回:\n{}", r);
            rct.response().end(r);
        };
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
            String url = GZHUtil.redirectUrl(uri, code);

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
