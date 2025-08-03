# curd-vertx

#### 项目介绍
tollge项目的数据访问模块，基于Vert.x JDBC Client实现的通用数据库访问层。该模块支持多种数据库驱动和连接池，通过标准JDBC接口提供完整的CRUD操作，兼容MySQL、PostgreSQL、Oracle、SQL Server等主流关系型数据库。

#### 核心特性
- **多数据库支持**：支持MySQL、PostgreSQL、Oracle、SQL Server等主流数据库
- **连接池管理**：集成HikariCP、C3P0等高性能连接池
- **完整CRUD支持**：支持count、page、list、one、operate、batch、transaction等操作
- **SQL模板引擎**：集成sql-engine，支持动态SQL生成和参数绑定
- **事务管理**：支持分布式事务和批量操作的事务控制
- **命名转换**：自动下划线转驼峰命名
- **批量操作**：支持批量插入、更新、删除

#### 依赖

需要Java 21及以上版本支持。
maven
```xml
<dependency>
    <groupId>com.tollge.modules</groupId>
    <artifactId>curd-vertx</artifactId>
    <version>0.1.0</version>
</dependency>
```
Gradle
```groovy
implementation 'com.tollge.modules:curd-vertx:0.1.0'
```

#### 快速开始

1. **添加依赖**
   在项目中引入curd-vertx模块

2. **配置tollge.yml**
   ```yaml
   jdbc:
     jdbcUrl: jdbc:postgresql://localhost:5432/your_database
     username: your_username
     password: your_password
     maximumPoolSize: 20
     minimumIdle: 2
     provider_class: io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider
   ```

3. **配置参数说明**
   所有配置参数均为Vert.x JDBC Client的标准配置：
   - `jdbcUrl`: JDBC连接字符串
   - `username`: 数据库用户名
   - `password`: 数据库密码
   - `maximumPoolSize`: 连接池最大连接数
   - `minimumIdle`: 连接池最小空闲连接数
   - `provider_class`: 数据源提供器类

#### 数据库配置示例

##### PostgreSQL
```yaml
jdbc:
  jdbcUrl: jdbc:postgresql://localhost:5432/your_database
  username: your_username
  password: your_password
  maximumPoolSize: 20
  minimumIdle: 2
  provider_class: io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider
```

##### MySQL
```yaml
jdbc:
  jdbcUrl: jdbc:mysql://localhost:3306/your_database?useSSL=false&serverTimezone=UTC
  username: your_username
  password: your_password
  maximumPoolSize: 20
  minimumIdle: 2
  provider_class: io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider
```

##### Oracle
```yaml
jdbc:
  jdbcUrl: jdbc:oracle:thin:@localhost:1521:ORCL
  username: your_username
  password: your_password
  maximumPoolSize: 20
  minimumIdle: 2
  provider_class: io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider
```

##### SQL Server
```yaml
jdbc:
  jdbcUrl: jdbc:sqlserver://localhost:1433;databaseName=your_database
  username: your_username
  password: your_password
  maximumPoolSize: 20
  minimumIdle: 2
  provider_class: io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider
```

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

#### 连接池配置详解

##### HikariCP配置
```yaml
jdbc:
  jdbcUrl: jdbc:postgresql://localhost:5432/your_database
  username: your_username
  password: your_password
  maximumPoolSize: 20
  minimumIdle: 2
  idleTimeout: 300000
  maxLifetime: 1800000
  connectionTimeout: 30000
  provider_class: io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider
```

##### C3P0配置
```yaml
jdbc:
  jdbcUrl: jdbc:postgresql://localhost:5432/your_database
  user: your_username
  password: your_password
  maxPoolSize: 20
  minPoolSize: 2
  initialPoolSize: 2
  provider_class: io.vertx.ext.jdbc.spi.impl.C3P0DataSourceProvider
```

#### 命名转换

自动将数据库下划线命名转换为Java驼峰命名：

| 数据库字段名 | Java属性名 |
|-------------|------------|
| user_name | userName |
| create_time | createTime |
| is_deleted | isDeleted |

#### 性能优化建议

1. **连接池配置**：根据并发量调整连接池参数
2. **批量操作**：使用batch操作减少网络往返
3. **索引优化**：为查询字段添加适当索引
4. **SQL优化**：使用EXPLAIN分析SQL执行计划
5. **连接池监控**：监控连接池状态，及时调整参数

#### 错误处理

模块提供统一的错误处理机制：

```java
// 在BizVerticle中处理错误
@Override
protected void errorHandler(Message<JsonObject> msg, Throwable cause) {
    if (cause instanceof SQLException) {
        msg.fail(400, "数据库错误: " + cause.getMessage());
    } else {
        msg.fail(500, "服务器内部错误");
    }
}
```

#### 完整示例

参考测试代码获取完整使用示例：
- [测试用例](src/test/java/test/curd/) - 完整CRUD操作示例
- [SQL映射文件](src/test/resources/mapper/) - SQL模板配置示例
