# tollge-modules

#### 项目介绍
tollge项目的模块
扩展tollge, 实现各种功能.

#### 安装教程

需要什么module, 请参看module的引入方式.

#### 已经实现的module

|组|具体实现|
|-|-|
|auth 鉴权|[auth-common](auth/auth-common) 鉴权的个性实现,可支持本地缓存, 分布式缓存|
|data 数据源|[curd-vertx](data/curd-vertx) 基于vertx支持的连接池个性实现|
|data 数据源|[curd-reactive-pg](data/curd-reactive-pg) 基于reactive-pg-client的连接池个性实现|
|job 任务| 暂无|
|oss 对象存储|[oss-qiniu](oss/oss-qiniu) 七牛对象存储|
|sms 短信|[sms-dayu](sms/sms-dayu) 大鱼短信|
|web 网站服务|[web-http](web/web-http) http服务|
|wechat 微信|[wechat-gzh](wechat/wechat-gzh) 公众号实现|

#### 参与贡献

欢迎大家提供module新实现. 方便自己, 方便大家!

