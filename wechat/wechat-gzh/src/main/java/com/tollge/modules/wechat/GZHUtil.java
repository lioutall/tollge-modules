package com.tollge.modules.wechat;

import com.tollge.common.util.Properties;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Slf4j
public class GZHUtil {
    static final String APPID = Properties.getString("wechat", "appId");
    static final String SECRET = Properties.getString("wechat", "secret");
    static final String WXNO = Properties.getString("wechat", "wechatNo");

    static final String WEB_URL = Properties.getString("wechat", "web.url");

    public static String redirectUrl(String uri, String param) {
        try {
            return "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
                    + APPID + "&redirect_uri=" + URLEncoder.encode(WEB_URL + uri, "UTF-8")
                    + "&response_type=code&scope=snsapi_base&state="
                    + param + "#wechat_redirect";
        } catch (UnsupportedEncodingException e) {
            log.error("URLEncoder.encode失败", e);
        }
        return "";
    }

    public static String commonRedirect(String uri, String param) {
        return WEB_URL + "/gzh/redirect?uri=" + uri + "&code=" + param;
    }

    public static String passiveTextResponse(String openId, String msg) {
        return "<xml>\n" +
                " <ToUserName><![CDATA[" + openId + "]]></ToUserName>\n" +
                " <FromUserName><![CDATA[" + WXNO + "]]></FromUserName>\n" +
                " <CreateTime>" + System.currentTimeMillis()/1000 + "</CreateTime>\n" +
                " <MsgType><![CDATA[text]]></MsgType>\n" +
                " <Content><![CDATA["+ msg + "]]></Content>\n" +
                " </xml>";
    }
}
