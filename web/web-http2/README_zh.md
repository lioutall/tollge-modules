# web-http

#### 项目介绍
tollge项目的模块
扩展tollge, 实现http server.

#### 依赖

需要JDK1.8及以上版本支持.   
maven
```
<dependency>
    <groupId>com.tollge.modules</groupId>
    <artifactId>web-http</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```
Gradle
```
compile 'com.tollge.modules:web-http:0.1.0-SNAPSHOT'
```

#### 用户指导

1. 增加依赖
2. 配置tollge.yml
```
application:
  context.path: /web
  http.port: 8080
```
content.path 是默认url的前缀, 所有请求都应该满足/web/**, 你可以配置成空字符, 这样你就不用受url约束.   
http.port 是http server监听的端口

3. 开发,见demo
- 提供Http注解, 相当于Controller
- AbstractRouter中的sendBizWithUser在添加auth组件后可用

#### 提供的功能

对http server提供的filter功能, filter的格式如下:
```
filters.http:
  21:
    pattern: /*
    class: io.vertx.ext.web.handler.ResponseContentTypeHandler
  22:
    pattern: /*
    class: io.vertx.ext.web.handler.CookieHandler
  23:
    pattern: /*
    class: io.vertx.ext.web.handler.BodyHandler
```
数字21, 22 是filter的加载顺序号, pattern 是filter监听的uri. class是创建过滤器的接口(原生的vertx规范).   
**这里约束自定义filter的加载顺序数字必须>20**, 小于20的顺序号预留给其他modules使用.   
下面是加载顺序占用表, 添加module请先占用再使用, 免得冲突

|顺序号|module|用途|
|-|-|-|
|15|auth-common|权限校验|

