package com.tollge.modules.wechat;

import com.tollge.common.annotation.Method;
import com.tollge.common.annotation.mark.Path;
import com.tollge.common.annotation.mark.request.Body;
import com.tollge.common.util.Properties;
import com.tollge.common.verticle.AbstractRouter;
import com.tollge.modules.web.http.Http;
import io.netty.util.internal.StringUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@NoArgsConstructor
@Http("/wechatPay")
@Slf4j
public class WechatPayRouter extends AbstractRouter {

  private static final String AES_KEY = Properties.getString("wechatPay", "aes.key");

    /**
     * 监听事件消息(扫码支付)
     *
     * @param rct []
     */
    @Path(value = "/event", method = Method.POST, description = "监听事件消息(扫码支付)")
    public AsyncResult<Void> dealerList(RoutingContext rct, @Body WechatPayEvent wechatPayEvent) {
      log.info("WechatPayRouter 监听到微信事件:{}", Json.encode(wechatPayEvent));

      // 解密
      if (wechatPayEvent.getResource() != null && !StringUtil.isNullOrEmpty(wechatPayEvent.getResource().getCiphertext())) {
        String resourceStr = AesUtil.decryptToString(wechatPayEvent.getResource().getAssociatedData().getBytes(StandardCharsets.UTF_8),
          wechatPayEvent.getResource().getNonce().getBytes(StandardCharsets.UTF_8),
          wechatPayEvent.getResource().getCiphertext(), AES_KEY.getBytes(StandardCharsets.UTF_8));

        ResourcePojo pojo = Json.decodeValue(resourceStr, ResourcePojo.class);

        return sendBiz(WechatPayVerticle.WEICHAT_PAY + WechatPayVerticle.CALLBACK, pojo);
      }

      return Future.failedFuture("");
    }

}
