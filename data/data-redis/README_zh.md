# data-redis

#### 项目介绍
tollge项目的模块, 扩展tollge提供redis调用功能.

#### 依赖

需要JDK1.8及以上版本支持.   
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

#### 用户指导

1. 增加依赖
2. 配置tollge.yml
```
redis:
  type: STANDALONE
  connectionString: redis://127.0.0.1:6379
```

3. 代码很少, 详细见demo.


