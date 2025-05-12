package com.tollge.modules.wechat;

import lombok.Data;

/**
 * 微信支付下单请求参数
 */
@Data
public class CreatePayRequest {
  private String description;          // 商品描述
  private String outTradeNo;    // 商户订单号
  private Integer totalFee;     // 总金额(单位:分)
  private String notifyUrl;
}
