# data-redis

##### [中文版](https://github.com/lioutall/tollge-modules/blob/master/data/data-redis/README_zh.md)
#### Introduction
A module of tollge specification, extended to providing redis call.

#### Dependency

Requires JDK1.8+.   
maven
```
<dependency>
    <groupId>com.tollge.modules</groupId>
    <artifactId>data-redis</artifactId>
    <version>0.1.0</version>
</dependency>
```
Gradle
```
compile 'com.tollge.modules:data-redis:0.1.0'
```

#### User Guide

1. Add maven dependency.
2. modify tollge.yml
```
redis:
  type: STANDALONE
  connectionString: redis://127.0.0.1:6379
```

3. Very few code, see demo for details.



