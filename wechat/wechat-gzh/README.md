# wechat-gzh

#### 项目介绍
tollge项目的模块
扩展tollge, 实现微信公众号功能.

#### 依赖

需要JDK1.8及以上版本支持.   
maven
```
<dependency>
    <groupId>com.tollge.modules</groupId>
    <artifactId>wechat-gzh</artifactId>
    <version>0.1.0</version>
</dependency>
```
Gradle
```
compile 'com.tollge.modules:wechat-gzh:0.1.0'
```

#### 用户指导

1. 增加依赖
2. 配置tollge.yml
```
wechat:
  web.url: http://www.abc.com
  event.token: token
  appId: sflkjvlkjslk
  wechatNo: gh_428asdfsdfv
  secret: sdfasdfvv123123fasdf1
```
- web.url 服务器url, 主要用于公众号鉴权成功后跳转
- event.token 微信参数
- appId,wechatNo,secret 微信参数

3. 提供的功能
- 提供 ${web.url}/event 来监听微信的回调事件
 - 定制 biz://gzh/event/scan 来处理扫码事件
 - 定制 biz://gzh/event/text 来处理文字事件
- 提供扫码跳转逻辑, 你只需要把url设置为: ${web.url}/redirect?uri=u&code=c   
这里的uri是指公众号验证成功后的跳转地址, code是参数
- 提供微信部分接口实现
 - GZHVerticle.getToken 获取有效的token(超时失效自动刷新)
 - third://gzh/code2accessCode 通过code换取网页授权网页access_token, 一个参数: code
 - third://gzh/userInfo 拉取用户信息
 - third://gzh/qrSCENE 临时二维码, 通过扫这个码来触发事件.  两个参数: sceneStr(事件获取的字符), expireSeconds(临时二维码的有效时间)
 - third://gzh/long2short 长链接转短链接接口 一个参数: url
 


