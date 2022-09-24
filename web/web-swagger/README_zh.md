# web-swagger

#### 项目介绍
tollge项目的模块
扩展tollge, 实现http server swagger.

#### 依赖

需要JDK1.8及以上版本支持.   
maven
```
<dependency>
    <groupId>com.tollge.modules</groupId>
    <artifactId>web-swagger</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```
Gradle
```
compile 'com.tollge.modules:web-swagger:0.1.0-SNAPSHOT'
```

#### 用户指导

1. 增加依赖, 配置开关(swagger.enable)开启, 配置监听端口(swagger.port)
2. Serve the Swagger JSON spec out on /swagger

