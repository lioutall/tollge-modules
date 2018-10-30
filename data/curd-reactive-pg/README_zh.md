# curd-reactive-pg

#### 项目介绍
tollge项目的模块, 扩展tollge提供数据功能.   
该module提供[reactive-pg-client](https://github.com/reactiverse/reactive-pg-client)实现.   
对sql实现定制, 使用的工具是[sql-engine](https://github.com/lioutall/sql-engine)   
实现Biz中的one, list, operate, batch...

#### 依赖

需要JDK1.8及以上版本支持.   
maven
```
<dependency>
    <groupId>com.tollge.modules</groupId>
    <artifactId>curd-reactive-pg</artifactId>
    <version>0.1.0</version>
</dependency>
```
Gradle
```
compile 'com.tollge.modules:curd-reactive-pg:0.1.0'
```

#### 用户指导

1. 增加依赖
2. 配置tollge.yml
```
jdbc:
  host: host
  port: 5432
  database: dbname
  user: dbuser
  password: pa*
  maxSize: 20
  maxWaitQueueSize: 2
  cachePreparedStatements: false
```
这里的参数全部是reactive-pg-client的参数, 其他的请看[reactive-pg-client的配置](https://github.com/reactiverse/reactive-pg-client/blob/master/docs/guide/java/index.md).


3. 代码很少, 详细见demo.


