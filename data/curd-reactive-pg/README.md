# curd-reactive-pg

##### [中文版](https://github.com/lioutall/tollge-modules/blob/master/data/curd-reactive-pg/README_zh.md)
#### Introduction
A module of tollge specification, extended to providing data function.   
It provide implementation via [reactive-pg-client](https://github.com/reactiverse/reactive-pg-client).   
Customize the sql implementation, the tools used are [sql-engine](https://github.com/lioutall/sql-engine)   
Implement methods of Biz like: one, list, operate, batch...

#### Dependency

Requires JDK1.8+.   
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

#### User Guide

1. Add maven dependency.
2. modify tollge.yml
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
The parameters here are all parameters of reactive-pg-client, see[reactive-pg-client guide](https://github.com/reactiverse/reactive-pg-client/blob/master/docs/guide/java/index.md).


3. Very few code, see demo for details.



