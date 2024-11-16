# data-redis

#### 项目介绍
tollge项目的模块, 扩展tollge提供meilisearch的调用功能.

#### 依赖

需要JDK1.8及以上版本支持.   
maven
```
<dependency>
    <groupId>com.tollge.modules</groupId>
    <artifactId>data-meilisearch</artifactId>
    <version>0.1.0</version>
</dependency>
```
Gradle
```
compile 'com.tollge.modules:data-meilisearch:0.1.0'
```

#### 用户指导

1. 增加依赖
2. 配置tollge.yml
```
meilisearch:
  host: http://127.0.0.1:7700
  masterKey: xxx
```

3. 代码很少, 详细见demo.


