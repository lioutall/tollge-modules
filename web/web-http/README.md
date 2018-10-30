# web-http

##### [中文版](https://github.com/lioutall/tollge-modules/blob/master/web/web-http/README_zh.md)
#### Introduction
A module of tollge specification, extended to providing server feature.   
It provide implementation of http server.

#### Dependency

Requires JDK1.8+.   
maven
```
<dependency>
    <groupId>com.tollge.modules</groupId>
    <artifactId>web-http</artifactId>
    <version>0.1.0</version>
</dependency>
```
Gradle
```
compile 'com.tollge.modules:web-http:0.1.0'
```

#### User Guide

1. Add maven dependency.
2. modify tollge.yml
```
application:
  context.path: /web
  http.port: 8080
```
content.path Is the default url prefix, all requests should satisfy /web/**, you can configure it as a null character, so you don't have to be bound by the url.   
http.port Is the port that the http server listens on.

3. More see demo.
- Provide `Http` annotation, equivalent to Controller
- `sendBizWithUser` in `AbstractRouter` is available after adding [auth component](https://github.com/lioutall/tollge-modules/tree/master/auth/auth-common).

#### 提供的功能

For the filter function provided by the http server, the format of the filter is as follows:
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
The number `21`, `22` is the load sequence number of the filter   
`pattern` is the uri of the filter listener..
`class` is the interface to create the filter (vertx code).   
**The constraint load number of the custom filter must >20**, Sequence numbers less than 20 are reserved for use by other modules.   
The following is the load order occupancy table, add the module, please use it first, and avoid conflicts.

|sequence|module|remark|
|-|-|-|
|15|auth-common|Permission check|


