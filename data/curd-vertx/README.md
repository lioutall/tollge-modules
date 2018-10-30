# curd-vertx

##### [中文版](https://github.com/lioutall/tollge-modules/blob/master/data/curd-vertx/README_zh.md)
#### Introduction
A module of tollge specification, extended to providing data function.   
This module provides some implementations of vertx. It can support multiple drivers and connection pools.   
Customize the sql implementation, the tools used are [sql-engine](https://github.com/lioutall/sql-engine)   
Implement methods of Biz like: one, list, operate, batch...

#### Dependency

Requires JDK1.8+.   
maven
```
<dependency>
    <groupId>com.tollge.modules</groupId>
    <artifactId>curd-vertx</artifactId>
    <version>0.1.0</version>
</dependency>
```
Gradle
```
compile 'com.tollge.modules:curd-vertx:0.1.0'
```

#### User Guide

1. Add maven dependency
2. modify tollge.yml
```
jdbc:
  jdbcUrl: jdbc:postgresql://ip:port/dbname
  maximumPoolSize: 20
  minimumIdle: 2
  password: dbpassword
  provider_class: io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider
  username: dbuser
```
The parameters here are all parameters of vertx. Please see [vertx doc](https://vertx.io/docs/vertx-jdbc-client/java/#_configuration)   
Note that the provider_class, you can choose the driver.


3. The code is very small, see demo.



