# curd-vertx

#### 项目介绍
tollge项目的模块, 扩展tollge提供数据功能.   
该module提供vertx的原生实现.可以支持多种驱动和连接池.   
对sql实现定制, 使用的工具是[sql-engine](https://github.com/lioutall/sql-engine)   
实现Biz中的one, list, operate, batch...

#### 依赖

需要JDK1.8及以上版本支持.   
maven
```
<dependency>
    <groupId>com.tollge.modules</groupId>
    <artifactId>auth-vertx</artifactId>
    <version>0.1.0</version>
</dependency>
```
Gradle
```
compile 'com.tollge.modules:auth-vertx:0.1.0'
```

#### 用户指导

1. 增加依赖
2. 配置tollge.yml
```
jdbc:
  jdbcUrl: jdbc:postgresql://ip:port/dbname
  maximumPoolSize: 20
  minimumIdle: 2
  password: dbpassword
  provider_class: io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider
  username: dbuser
```
这里的参数全部是vertx的原生参数, 其他的请看[vertx的配置](https://vertx.io/docs/vertx-jdbc-client/java/#_configuration)
要注意的是provider_class, 可以自行选择驱动.


3. 代码很少, 详细见demo.


