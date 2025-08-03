# curd-reactive-pg

#### 项目介绍
tollge项目的数据访问模块，基于Reactive PostgreSQL Client实现的高性能异步数据库访问层。该模块提供了完整的CRUD操作支持，包括分页查询、事务处理、批量操作等功能，完全集成tollge框架的数据访问规范。

#### 核心特性
- **响应式编程**：基于Vert.x Reactive PostgreSQL Client，提供非阻塞异步操作
- **完整CRUD支持**：支持count、page、list、one、operate、batch、transaction等操作
- **SQL模板引擎**：集成sql-engine，支持动态SQL生成和参数绑定
- **自动分页**：内置分页查询支持，自动处理limit和offset
- **事务管理**：支持分布式事务和批量操作的事务控制
- **连接池管理**：内置连接池，支持高并发场景
- **类型转换**：自动下划线转驼峰命名，支持复杂类型映射

#### 依赖

需要Java 21及以上版本支持。
maven
```xml
<dependency>
    <groupId>com.tollge.modules</groupId>
    <artifactId>curd-reactive-pg</artifactId>
    <version>0.1.0</version>
</dependency>
```
Gradle
```groovy
implementation 'com.tollge.modules:curd-reactive-pg:0.1.0'
```

#### 快速开始

1. **添加依赖**
   在项目中引入curd-reactive-pg模块

2. **配置tollge.yml**
   ```yaml
   jdbc:
     host: localhost
     port: 5432
     database: your_database
     user: your_username
     password: your_password
     maxSize: 20
     maxWaitQueueSize: 2
     cachePreparedStatements: false
   ```

3. **配置参数说明**
   所有配置参数均为reactive-pg-client的标准配置：
   - `host`: PostgreSQL服务器地址
   - `port`: PostgreSQL端口，默认5432
   - `database`: 数据库名称
   - `user`: 数据库用户名
   - `password`: 数据库密码
   - `maxSize`: 连接池最大连接数
   - `maxWaitQueueSize`: 等待队列最大长度
   - `cachePreparedStatements`: 是否缓存预编译语句

#### 支持的SQL操作

模块支持以下标准操作类型：

| 操作类型 | 方法名 | 说明 |
|---------|--------|------|
| COUNT | count | 统计记录数量 |
| PAGE | page | 分页查询 |
| LIST | list | 列表查询 |
| ONE | one | 单条记录查询 |
| OPERATE | operate | 增删改操作 |
| BATCH | batch | 批量操作 |
| TRANSACTION | transaction | 事务操作 |

#### 使用示例

##### 1. 基本查询操作

```java
@Biz("biz://user")
public class UserBiz extends BizVerticle {
    
    @Path("/count")
    public void countUsers(Message<JsonObject> msg) {
        SqlAndParams sqlAndParams = new SqlAndParams("user.count");
        count(msg, sqlAndParams);
    }
    
    @Path("/list")
    public void listUsers(Message<JsonObject> msg) {
        JsonObject params = msg.body();
        SqlAndParams sqlAndParams = new SqlAndParams("user.list")
            .putParam("status", params.getString("status"));
        list(msg, sqlAndParams, User.class);
    }
    
    @Path("/page")
    public void pageUsers(Message<JsonObject> msg) {
        JsonObject params = msg.body();
        SqlAndParams sqlAndParams = new SqlAndParams("user.page")
            .putParam("name", params.getString("name"));
        // pageNum和pageSize自动从msg.body获取
        list(msg, sqlAndParams, User.class);
    }
    
    @Path("/getById")
    public void getUserById(Message<JsonObject> msg) {
        String userId = msg.body().getString("userId");
        SqlAndParams sqlAndParams = new SqlAndParams("user.getById")
            .putParam("id", userId);
        one(msg, sqlAndParams, User.class);
    }
}
```

##### 2. 增删改操作

```java
@Path("/create")
public void createUser(Message<JsonObject> msg) {
    JsonObject user = msg.body();
    SqlAndParams sqlAndParams = new SqlAndParams("user.create")
        .putParam("name", user.getString("name"))
        .putParam("email", user.getString("email"))
        .putParam("age", user.getInteger("age"));
    operate(msg, sqlAndParams);
}

@Path("/update")
public void updateUser(Message<JsonObject> msg) {
    JsonObject user = msg.body();
    SqlAndParams sqlAndParams = new SqlAndParams("user.update")
        .putParam("id", user.getString("id"))
        .putParam("name", user.getString("name"))
        .putParam("email", user.getString("email"));
    operate(msg, sqlAndParams);
}

@Path("/delete")
public void deleteUser(Message<JsonObject> msg) {
    String userId = msg.body().getString("userId");
    SqlAndParams sqlAndParams = new SqlAndParams("user.delete")
        .putParam("id", userId);
    operate(msg, sqlAndParams);
}
```

##### 3. 批量操作

```java
@Path("/batchCreate")
public void batchCreateUsers(Message<JsonObject> msg) {
    JsonArray users = msg.body().getJsonArray("users");
    List<SqlAndParams> batchParams = new ArrayList<>();
    
    for (int i = 0; i < users.size(); i++) {
        JsonObject user = users.getJsonObject(i);
        SqlAndParams sqlAndParams = new SqlAndParams("user.create")
            .putParam("name", user.getString("name"))
            .putParam("email", user.getString("email"));
        batchParams.add(sqlAndParams);
    }
    
    batch(msg, batchParams);
}
```

##### 4. 事务操作

```java
@Path("/transfer")
public void transferMoney(Message<JsonObject> msg) {
    JsonObject params = msg.body();
    String fromAccount = params.getString("fromAccount");
    String toAccount = params.getString("toAccount");
    Double amount = params.getDouble("amount");
    
    List<SqlAndParams> transactionOps = Arrays.asList(
        new SqlAndParams("account.subtract")
            .putParam("accountId", fromAccount)
            .putParam("amount", amount),
        new SqlAndParams("account.add")
            .putParam("accountId", toAccount)
            .putParam("amount", amount)
    );
    
    transaction(msg, transactionOps, result -> {
        if (result.succeeded()) {
            msg.reply(new JsonObject().put("success", true));
        } else {
            msg.fail(500, "转账失败: " + result.cause().getMessage());
        }
    });
}
```

#### SQL模板配置

使用sql-engine进行SQL模板管理，在resources/mapper目录下创建SQL映射文件：

```xml
<!-- user.xml -->
<mapper namespace="user">
    <select id="count">
        SELECT COUNT(*) FROM users
    </select>
    
    <select id="list">
        SELECT * FROM users 
        WHERE status = #{status}
        ORDER BY create_time DESC
    </select>
    
    <select id="page">
        SELECT * FROM users 
        WHERE name LIKE #{name}
        ORDER BY create_time DESC
    </select>
    
    <select id="getById">
        SELECT * FROM users WHERE id = #{id}
    </select>
    
    <insert id="create">
        INSERT INTO users(name, email, age) 
        VALUES(#{name}, #{email}, #{age})
    </insert>
    
    <update id="update">
        UPDATE users SET 
        name = #{name}, 
        email = #{email}
        WHERE id = #{id}
    </update>
    
    <delete id="delete">
        DELETE FROM users WHERE id = #{id}
    </delete>
</mapper>
```

#### 数据类型映射

支持以下数据类型自动转换：

| PostgreSQL类型 | Java类型 |
|---------------|----------|
| INTEGER | Integer |
| BIGINT | Long |
| NUMERIC | BigDecimal |
| VARCHAR | String |
| TEXT | String |
| TIMESTAMP | LocalDateTime |
| DATE | LocalDate |
| BOOLEAN | Boolean |
| JSON | JsonObject |
| JSONB | JsonObject |

#### 命名转换

自动将数据库下划线命名转换为Java驼峰命名：

| 数据库字段名 | Java属性名 |
|-------------|------------|
| user_name | userName |
| create_time | createTime |
| is_deleted | isDeleted |

#### 连接池监控

可以通过以下方式监控连接池状态：

```java
// 获取当前活跃连接数
int activeConnections = dao.getActiveConnections();

// 获取空闲连接数
int idleConnections = dao.getIdleConnections();

// 获取等待队列长度
int waitQueueSize = dao.getWaitQueueSize();
```

#### 性能优化建议

1. **连接池配置**：根据并发量调整maxSize参数
2. **预编译缓存**：开启cachePreparedStatements提升性能
3. **批量操作**：使用batch操作减少网络往返
4. **索引优化**：为查询字段添加适当索引
5. **分页优化**：大数据量分页使用游标或延迟游标

#### 错误处理

模块提供统一的错误处理机制：

```java
// 在BizVerticle中处理错误
@Override
protected void errorHandler(Message<JsonObject> msg, Throwable cause) {
    if (cause instanceof SqlEngineException) {
        msg.fail(400, "SQL错误: " + cause.getMessage());
    } else {
        msg.fail(500, "服务器内部错误");
    }
}
```

#### 完整示例

参考测试代码获取完整使用示例：
- [测试用例](src/test/java/test/curd/) - 完整CRUD操作示例
- [SQL映射文件](src/test/resources/mapper/) - SQL模板配置示例
