# wechat-gzh

#### 项目介绍
tollge项目的模块
扩展tollge, 实现微信商户号功能.

#### 依赖

需要JDK1.8及以上版本支持.
maven
```
<dependency>
    <groupId>com.tollge.modules</groupId>
    <artifactId>wechat-pay</artifactId>
    <version>0.1.0</version>
</dependency>
```
Gradle
```
compile 'com.tollge.modules:wechat-pay:0.1.0'
```

#### 用户指导

1. 增加依赖
2. 配置tollge.yml
```
wechat:
  pay.appid: appid
  pay.mchid: mchid
  pay.host: www.wechat.com
  pay.isSSL: true
  pay.port: 443
```
- pay.notify_url 服务器url, 主要用于成功后跳转
- appId,mchid,host 微信参数

3. 提供的功能
- 提供 ${web.url}/event 来监听微信的回调事件
 - 用户定制实现 WechatPayVerticle.WEICHAT_PAY + WechatPayVerticle.CALLBACK 来处理扫码事件
这里的uri是指公众号验证成功后的跳转地址, code是参数
- 提供微信部分接口实现
 - WechatPayVerticle.WEICHAT_PAY + WechatPayVerticle.CREATE_PAY 调用该接口在微信支付下单，生成用于调起支付的二维码链接code_url
 - WechatPayVerticle.WEICHAT_PAY + WechatPayVerticle.QUERY 使用商户订单号查询订单
