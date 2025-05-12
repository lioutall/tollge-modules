package com.tollge.modules.wechat;

import com.tollge.common.StatusCodeMsg;
import com.tollge.common.annotation.mark.Biz;
import com.tollge.common.annotation.mark.Path;
import com.tollge.common.util.Properties;
import com.tollge.common.verticle.BizVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import lombok.extern.slf4j.Slf4j;

/**
 * 微信公众号
 */
@Slf4j
@Biz(value = WechatPayVerticle.WEICHAT_PAY, instances = 1)
public class WechatPayVerticle extends BizVerticle {

  private static final String APPID = Properties.getString("wechat", "pay.appid");
  private static final String MCHID = Properties.getString("wechat", "pay.mchid");
  private static final String API_WEIXIN_QQ_COM = Properties.getString("wechat", "pay.host");
  private static final boolean API_WEIXIN_QQ_COM_SSL = Properties.getBoolean("wechat", "pay.isSSL");
  private static final int API_WEIXIN_QQ_COM_PORT = Properties.getInteger("wechat", "pay.port");

  public static final String WEICHAT_PAY = "third://wechatPay";
  public static final String CREATE_PAY = "/createPay";
  public static final String QUERY = "/query";
  public static final String CALLBACK = "/callback";

  /**
   * 调用该接口在微信支付下单，生成用于调起支付的二维码链接code_url
   *
   * @param msg [body: {out_trade_no, total_fee, body, attach}]
   */
  @Path(CREATE_PAY)
  public void createPay(Message<CreatePayRequest> msg) {
    // 将JsonObject转换为POJO对象
    CreatePayRequest request = msg.body();

    WebClient client = WebClient.create(vertx);

    // 使用POJO对象属性构建请求体
    JsonObject amount = new JsonObject()
            .put("total", request.getTotalFee())
            .put("currency", "CNY");

    JsonObject requestBody = new JsonObject()
            .put("appid", APPID)
            .put("mchid", MCHID)
            .put("description", request.getDescription())
            .put("out_trade_no", request.getOutTradeNo())
            .put("notify_url", request.getNotifyUrl())
            .put("amount", amount);

    // 发送POST请求
    client.post(API_WEIXIN_QQ_COM_PORT, API_WEIXIN_QQ_COM, "/v3/pay/transactions/native")
            .ssl(API_WEIXIN_QQ_COM_SSL)
            .putHeader("Authorization", "WECHATPAY2-SHA256-RSA2048 mchid=\"" + MCHID + "\",...")
            .putHeader("Accept", "application/json")
            .putHeader("Content-Type", "application/json")
            .sendJsonObject(requestBody, res -> {
                if (res.succeeded()) {
                    msg.reply(res.result().bodyAsJsonObject());
                } else {
                    log.error("createPay failed", res.cause());
                    msg.fail(StatusCodeMsg.C404.getCode(), res.cause().getMessage());
                }
            });
  }

  /**
   * 使用商户订单号查询订单
   *
   * @param msg [body: {out_trade_no}]
   */
  @Path(QUERY)
  public void query(Message<JsonObject> msg) {
    JsonObject jo = msg.body();
    WebClient client = WebClient.create(vertx);

    String outTradeNo = jo.getString("out_trade_no");
    String urlPath = "/v3/pay/transactions/out-trade-no/" + outTradeNo + "?mchid=" + MCHID;

    // 发送GET请求
    client.get(API_WEIXIN_QQ_COM_PORT, API_WEIXIN_QQ_COM, urlPath)
            .ssl(API_WEIXIN_QQ_COM_SSL)
            .putHeader("Authorization", "WECHATPAY2-SHA256-RSA2048 mchid=\"" + MCHID + "\"")
            .putHeader("Accept", "application/json")
            .send(res -> {
                if (res.succeeded()) {
                    msg.reply(res.result().bodyAsJsonObject());
                } else {
                    log.error("query failed", res.cause());
                    msg.fail(StatusCodeMsg.C404.getCode(), res.cause().getMessage());
                }
            });
  }

  /**
   * 支付回调
   */
  /*@Path(CALLBACK)
  public void callback(Message<JsonObject> msg) {

  }*/


}
