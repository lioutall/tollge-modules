# data-redis

#### 项目介绍
tollge项目的Redis数据访问模块，基于Vert.x Redis Client实现的高性能Redis客户端。该模块提供了完整的Redis操作支持，包括字符串操作、哈希操作、列表操作、集合操作、有序集合操作，以及分布式锁和缓存封装功能。

#### 核心特性
- **完整Redis命令支持**：支持所有Redis核心命令
- **分布式锁**：基于Redis的分布式锁实现
- **缓存封装**：提供缓存操作的高级封装
- **连接管理**：自动连接管理和重连机制
- **多种部署模式**：支持单机、哨兵、集群模式
- **异步操作**：基于Future的异步API设计
- **批量操作**：支持批量缓存操作

#### 依赖

需要Java 21及以上版本支持。
maven
```xml
<dependency>
    <groupId>com.tollge.modules</groupId>
    <artifactId>data-redis</artifactId>
    <version>0.1.0</version>
</dependency>
```
Gradle
```groovy
implementation 'com.tollge.modules:data-redis:0.1.0'
```

#### 快速开始

1. **添加依赖**
   在项目中引入data-redis模块

2. **配置tollge.yml**
   ```yaml
   redis:
     type: STANDALONE
     connectionString: redis://127.0.0.1:6379
   ```

3. **配置参数说明**
   - `type`: Redis部署模式，可选值：STANDALONE、SENTINEL、CLUSTER
   - `connectionString`: Redis连接字符串

#### 部署模式配置

##### 单机模式
```yaml
redis:
  type: STANDALONE
  connectionString: redis://127.0.0.1:6379
```

##### 哨兵模式
```yaml
redis:
  type: SENTINEL
  connectionString: redis://sentinel1:26379,redis://sentinel2:26379,redis://sentinel3:26379
```

##### 集群模式
```yaml
redis:
  type: CLUSTER
  connectionString: redis://node1:6379,redis://node2:6379,redis://node3:6379
```

#### 基本操作

##### 字符串操作
```java
// 设置值
MyRedis.set("key", "value");

// 设置带过期时间的值
MyRedis.set("key", "value", 60000); // 60秒过期

// 获取值
MyRedis.get("key").onSuccess(value -> {
    System.out.println("Value: " + value);
});

// 获取值或默认值
MyRedis.getOrDefault("key", "default_value");

// 删除键
MyRedis.del("key");

// 设置过期时间
MyRedis.expire("key", 60); // 60秒后过期

// 获取剩余过期时间
MyRedis.ttl("key");

// 自增操作
MyRedis.incr("counter");
```

##### 批量操作
```java
// 批量获取
List<String> keys = Arrays.asList("key1", "key2", "key3");
MyRedis.mget(keys).onSuccess(values -> {
    // 处理返回的值列表
});

// 批量设置
Map<String, String> keyValues = new HashMap<>();
keyValues.put("key1", "value1");
keyValues.put("key2", "value2");
MyRedis.mset(keyValues);
```

##### 哈希操作
```java
// 设置哈希字段
MyRedis.getClient().hset(Arrays.asList("user:1", "name", "张三", "age", "25"));

// 获取哈希字段
MyRedis.getClient().hget("user:1", "name");

// 获取所有哈希字段
MyRedis.getClient().hgetall("user:1");

// 删除哈希字段
MyRedis.getClient().hdel("user:1", "name");
```

##### 列表操作
```java
// 左推入列表
MyRedis.getClient().lpush(Arrays.asList("list:1", "value1", "value2"));

// 右推入列表
MyRedis.getClient().rpush(Arrays.asList("list:1", "value3"));

// 获取列表范围
MyRedis.getClient().lrange(Arrays.asList("list:1", "0", "-1"));

// 弹出列表元素
MyRedis.getClient().lpop("list:1");
```

##### 集合操作
```java
// 添加集合成员
MyRedis.getClient().sadd(Arrays.asList("set:1", "member1", "member2"));

// 获取集合成员
MyRedis.getClient().smembers("set:1");

// 移除集合成员
MyRedis.getClient().srem(Arrays.asList("set:1", "member1"));

// 检查成员是否存在
MyRedis.getClient().sismember(Arrays.asList("set:1", "member1"));
```

##### 有序集合操作
```java
// 添加有序集合成员
MyRedis.getClient().zadd(Arrays.asList("zset:1", "1", "member1", "2", "member2"));

// 获取有序集合范围
MyRedis.getClient().zrange(Arrays.asList("zset:1", "0", "-1"));

// 获取有序集合分数
MyRedis.getClient().zscore(Arrays.asList("zset:1", "member1"));
```

#### 分布式锁

##### 基本使用
```java
// 获取分布式锁
String lockKey = "lock:resource";
String requestId = UUID.randomUUID().toString();
int expireTime = 5000; // 5秒过期

MyRedis.tryGetDistributedLock(lockKey, requestId, expireTime)
    .onSuccess(result -> {
        if ("OK".equals(result.toString())) {
            // 获取锁成功
            try {
                // 执行业务逻辑
                System.out.println("获取锁成功，执行业务逻辑");
            } finally {
                // 释放锁
                MyRedis.releaseDistributedLock(lockKey, requestId);
            }
        } else {
            // 获取锁失败
            System.out.println("获取锁失败");
        }
    });

// 简化版本，使用线程名作为requestId
MyRedis.tryGetDistributedLock(lockKey).onSuccess(result -> {
    // 处理结果
});

// 简化版本，使用默认过期时间
MyRedis.tryGetDistributedLock(lockKey, requestId).onSuccess(result -> {
    // 处理结果
});
```

##### 分布式锁最佳实践
```java
public class DistributedLockExample {
    
    public void executeWithLock(String resourceId) {
        String lockKey = "lock:resource:" + resourceId;
        String requestId = UUID.randomUUID().toString();
        int expireTime = 5000; // 5秒
        
        MyRedis.tryGetDistributedLock(lockKey, requestId, expireTime)
            .compose(result -> {
                if ("OK".equals(result.toString())) {
                    return executeBusinessLogic(resourceId)
                        .compose(businessResult -> 
                            MyRedis.releaseDistributedLock(lockKey, requestId)
                                .map(v -> businessResult)
                        );
                } else {
                    return Future.failedFuture("无法获取锁");
                }
            })
            .onSuccess(result -> {
                System.out.println("业务执行成功: " + result);
            })
            .onFailure(error -> {
                System.err.println("业务执行失败: " + error.getMessage());
            });
    }
    
    private Future<String> executeBusinessLogic(String resourceId) {
        // 实际业务逻辑
        return Future.succeededFuture("处理结果");
    }
}
```

#### 缓存封装

##### 单对象缓存
```java
// 缓存字符串
MyRedis.cache("user:1", "user:", 3600, 
    MyRedis.get("user:1:db"))
    .onSuccess(cachedValue -> {
        System.out.println("缓存值: " + cachedValue);
    });

// 缓存对象
MyRedis.cache("user:1", "user:", 3600, User.class,
    getUserFromDatabase("1"))
    .onSuccess(user -> {
        System.out.println("缓存用户: " + user.getName());
    });

// 自定义缓存逻辑
public Future<User> getUserWithCache(String userId) {
    return MyRedis.cache(userId, "user:", 3600, User.class,
        getUserFromDatabase(userId));
}
```

##### 批量缓存
```java
// 批量缓存对象
List<String> userIds = Arrays.asList("1", "2", "3");
MyRedis.cacheList(userIds, "user:", 3600, User.class,
    missingIds -> getUsersFromDatabase(missingIds))
    .onSuccess(users -> {
        System.out.println("批量获取用户: " + users.size());
    });

// 批量缓存字符串
List<String> keys = Arrays.asList("key1", "key2", "key3");
MyRedis.cacheList(keys, "cache:", 1800, String.class,
    missingKeys -> getValuesFromDatabase(missingKeys))
    .onSuccess(values -> {
        System.out.println("批量获取值: " + values);
    });
```

#### 高级功能

##### 发布订阅
```java
// 发布消息
MyRedis.getClient().publish(Arrays.asList("channel:1", "Hello World"));

// 订阅消息
MyRedis.getClient().subscribe(Arrays.asList("channel:1"));
```

##### 管道操作
```java
// 使用管道批量执行命令
MyRedis.getClient().pipeline()
    .set(Arrays.asList("key1", "value1"))
    .set(Arrays.asList("key2", "value2"))
    .get("key1")
    .get("key2");
```

##### 事务操作
```java
// 使用MULTI/EXEC事务
MyRedis.getClient().multi()
    .compose(v -> MyRedis.getClient().set(Arrays.asList("key1", "value1")))
    .compose(v -> MyRedis.getClient().set(Arrays.asList("key2", "value2")))
    .compose(v -> MyRedis.getClient().exec());
```

#### 配置详解

##### 连接配置
```yaml
redis:
  type: STANDALONE
  connectionString: redis://127.0.0.1:6379
  # 可选配置
  maxPoolSize: 10
  maxPoolWaiting: 20
  maxWaitingHandlers: 1000
  poolRecycleTimeout: 15000
```

##### 哨兵配置
```yaml
redis:
  type: SENTINEL
  connectionString: redis://sentinel1:26379,redis://sentinel2:26379,redis://sentinel3:26379
  masterName: mymaster
  role: MASTER
```

##### 集群配置
```yaml
redis:
  type: CLUSTER
  connectionString: redis://node1:6379,redis://node2:6379,redis://node3:6379
  maxPoolSize: 10
  maxPoolWaiting: 20
```

#### 错误处理

```java
// 处理Redis操作错误
MyRedis.get("key")
    .onSuccess(value -> {
        System.out.println("获取成功: " + value);
    })
    .onFailure(error -> {
        System.err.println("获取失败: " + error.getMessage());
        // 降级处理
    });

// 超时处理
MyRedis.get("key")
    .onComplete(ar -> {
        if (ar.succeeded()) {
            // 处理成功结果
        } else {
            // 处理失败，可能是网络问题或Redis不可用
        }
    });
```

#### 性能优化建议

1. **连接池配置**：根据并发量调整连接池大小
2. **键命名规范**：使用统一的键命名规范，避免冲突
3. **过期时间**：为缓存数据设置合理的过期时间
4. **批量操作**：使用批量操作减少网络往返
5. **数据压缩**：对大value进行压缩存储
6. **连接复用**：使用连接池避免频繁创建连接

#### 监控和诊断

```java
// 获取Redis信息
MyRedis.getClient().info().onSuccess(info -> {
    System.out.println("Redis信息: " + info);
});

// 获取客户端列表
MyRedis.getClient().client("list").onSuccess(clients -> {
    System.out.println("客户端列表: " + clients);
});

// 获取内存使用
MyRedis.getClient().memory("usage", "key").onSuccess(memory -> {
    System.out.println("内存使用: " + memory);
});
```

#### 完整示例

参考测试代码获取完整使用示例：
- [测试用例](src/test/java/test/redis/) - 完整Redis操作示例
- [配置示例](src/test/resources/) - 各种部署模式配置示例
