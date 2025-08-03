# tollge-modules

##### [中文版](https://github.com/lioutall/tollge-modules/blob/master/README_zh.md)
#### Introduction
Modules of tollge specification.
Extend tollge to implement various functions.

#### User Guide

What module is needed, please refer to the introduction method of module.

#### Implemented module

|group|implement|
|-|-|
|auth Authentication|[auth-common](auth/auth-common) Authentication personality implementation, can support local cache, distributed cache|
|data Datasource|[curd-vertx](data/curd-vertx) Personalization of connection pool based on vertx support|
|data Datasource|[curd-reactive-pg](data/curd-reactive-pg) Personalization of connection pool based on reactive-pg-client|
|job Task| None|
|oss Object storage|[oss-qiniu](oss/oss-qiniu) 七牛对象存储|
|sms SMS|[sms-dayu](sms/sms-dayu) 大鱼短信|
|web Web Server|[web-http](web/web-http) http server|
|wechat 微信|[wechat-gzh](wechat/wechat-gzh) 公众号实现|

#### Contribution

Welcome everyone to provide a new module implementation.

