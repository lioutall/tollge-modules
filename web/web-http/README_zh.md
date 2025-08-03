# web-http

#### 项目介绍
tollge项目的HTTP Web服务器模块，基于Vert.x Web实现的高性能HTTP服务器。该模块提供了完整的Web开发功能，包括路由管理、过滤器链、异常处理、静态资源服务等，支持RESTful API开发。

#### 核心特性
- **高性能HTTP服务器**：基于Vert.x Web，支持高并发处理
- **注解式路由**：通过@Http注解和@Path注解定义路由
- **过滤器链**：支持可配置的过滤器链，类似Spring的拦截器
- **RESTful支持**：完整支持RESTful API设计规范
- **异常处理**：统一的异常处理和响应格式
- **静态资源**：支持静态资源服务
- **内容协商**：支持多种内容类型（JSON、XML等）

#### 依赖

需要Java 21及以上版本支持。
maven
```xml
<dependency>
    <groupId>com.tollge.modules</groupId>
    <artifactId>web-http</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```
Gradle
```groovy
implementation 'com.tollge.modules:web-http:0.1.0-SNAPSHOT'
```

#### 快速开始

1. **添加依赖**
   在项目中引入web-http模块

2. **配置tollge.yml**
   ```yaml
   application:
     context.path: /api
     http.port: 8080
   ```

3. **配置参数说明**
   - `context.path`: URL上下文路径前缀，所有请求都会加上此前缀
   - `http.port`: HTTP服务器监听端口

#### 基本配置

##### 服务器配置
```yaml
application:
  context.path: /api
  http.port: 8080
  http.host: 0.0.0.0
```

##### 高级配置
```yaml
application:
  context.path: /api
  http.port: 8080
  http.host: 0.0.0.0
  http.maxHeaderSize: 8192
  http.maxChunkSize: 8192
  http.maxInitialLineLength: 4096
```

#### 路由定义

##### 1. 创建路由类
```java
@Http("/user")
public class UserRouter extends AbstractRouter {
    
    @Path("/:id")
    @Method(Method.GET)
    public Future<JsonObject> getUser(RoutingContext ctx, @PathParam("id") String userId) {
        return sendBiz("biz://user.getById", new JsonObject().put("userId", userId));
    }
    
    @Path("/")
    @Method(Method.POST)
    public Future<JsonObject> createUser(RoutingContext ctx, @Body User user) {
        return sendBiz("biz://user.create", JsonObject.mapFrom(user));
    }
    
    @Path("/:id")
    @Method(Method.PUT)
    public Future<JsonObject> updateUser(RoutingContext ctx, 
                                        @PathParam("id") String userId,
                                        @Body User user) {
        JsonObject params = JsonObject.mapFrom(user).put("userId", userId);
        return sendBiz("biz://user.update", params);
    }
    
    @Path("/:id")
    @Method(Method.DELETE)
    public Future<JsonObject> deleteUser(RoutingContext ctx, @PathParam("id") String userId) {
        return sendBiz("biz://user.delete", new JsonObject().put("userId", userId));
    }
    
    @Path("/")
    @Method(Method.GET)
    public Future<JsonObject> listUsers(RoutingContext ctx,
                                       @QueryParam("page") Integer page,
                                       @QueryParam("size") Integer size,
                                       @QueryParam("name") String name) {
        JsonObject params = new JsonObject()
            .put("pageNum", page != null ? page : 1)
            .put("pageSize", size != null ? size : 10)
            .put("name", name);
        return sendBiz("biz://user.page", params);
    }
}
```

##### 2. 路由注解说明
- `@Http`: 定义路由类的基础路径
- `@Path`: 定义具体的路由路径
- `@Method`: 定义HTTP方法（GET、POST、PUT、DELETE等）

##### 3. 参数绑定
```java
@Path("/search")
@Method(Method.GET)
public Future<JsonObject> searchUsers(RoutingContext ctx,
                                    @QueryParam("name") String name,
                                    @QueryParam("age") Integer age,
                                    @QueryParam("email") String email) {
    JsonObject params = new JsonObject()
        .put("name", name)
        .put("age", age)
        .put("email", email);
    return sendBiz("biz://user.search", params);
}

@Path("/profile")
@Method(Method.GET)
public Future<JsonObject> getUserProfile(RoutingContext ctx,
                                       @HeaderParam("Authorization") String token) {
    return sendBiz("biz://user.profile", new JsonObject().put("token", token));
}

@Path("/upload")
@Method(Method.POST)
public Future<JsonObject> uploadFile(RoutingContext ctx,
                                   @FormParam("file") String fileName,
                                   @FormParam("description") String description) {
    // 处理文件上传
    return sendBiz("biz://file.upload", new JsonObject()
        .put("fileName", fileName)
        .put("description", description));
}
```

#### 过滤器配置

##### 内置过滤器
模块提供了以下内置过滤器：

| 顺序号 | 过滤器类 | 功能说明 |
|--------|----------|----------|
| 21 | ResponseContentTypeHandler | 响应内容类型处理 |
| 22 | CookieHandler | Cookie处理 |
| 23 | BodyHandler | 请求体处理 |

##### 自定义过滤器
可以添加自定义过滤器，顺序号必须大于20：

```yaml
filters.http:
  25:
    pattern: /api/*
    class: com.yourcompany.filter.LoggingFilter
  26:
    pattern: /admin/*
    class: com.yourcompany.filter.AdminAuthFilter
```

##### 过滤器实现示例
```java
public class LoggingFilter implements Handler<RoutingContext> {
    
    @Override
    public void handle(RoutingContext ctx) {
        // 记录请求日志
        System.out.println("Request: " + ctx.request().method() + " " + ctx.request().path());
        
        // 继续处理
        ctx.next();
    }
    
    public static LoggingFilter create() {
        return new LoggingFilter();
    }
}

public class AuthFilter implements Handler<RoutingContext> {
    
    @Override
    public void handle(RoutingContext ctx) {
        String token = ctx.request().getHeader("Authorization");
        if (token == null || !isValidToken(token)) {
            ctx.response()
                .setStatusCode(401)
                .end("Unauthorized");
            return;
        }
        
        // 设置用户信息到上下文
        ctx.put("user", getUserFromToken(token));
        ctx.next();
    }
    
    public static AuthFilter create() {
        return new AuthFilter();
    }
}
```

#### 过滤器顺序表

| 顺序号 | 模块 | 用途 |
|--------|------|------|
| 15 | auth-common | 权限校验 |
| 21-23 | web-http | 内置过滤器 |
| 25+ | 自定义 | 用户自定义过滤器 |

#### 异常处理

##### 全局异常处理
```java
@Http("/api")
public class ApiRouter extends AbstractRouter {
    
    @Override
    protected void handleException(RoutingContext ctx, Throwable error) {
        if (error instanceof ValidationException) {
            ctx.response()
                .setStatusCode(400)
                .end(new JsonObject()
                    .put("code", 400)
                    .put("message", error.getMessage())
                    .encode());
        } else if (error instanceof NotFoundException) {
            ctx.response()
                .setStatusCode(404)
                .end(new JsonObject()
                    .put("code", 404)
                    .put("message", "Resource not found")
                    .encode());
        } else {
            ctx.response()
                .setStatusCode(500)
                .end(new JsonObject()
                    .put("code", 500)
                    .put("message", "Internal server error")
                    .encode());
        }
    }
}
```

##### 路由级异常处理
```java
@Path("/users/:id")
@Method(Method.GET)
public Future<JsonObject> getUser(RoutingContext ctx, @PathParam("id") String userId) {
    return sendBiz("biz://user.getById", new JsonObject().put("userId", userId))
        .recover(error -> {
            if (error instanceof NotFoundException) {
                return Future.failedFuture(new WebException(404, "User not found"));
            }
            return Future.failedFuture(error);
        });
}
```

#### 静态资源服务

##### 配置静态资源
```yaml
filters.http:
  30:
    pattern: /static/*
    class: io.vertx.ext.web.handler.StaticHandler
```

##### 自定义静态资源处理器
```java
@Http("/")
public class StaticRouter extends AbstractRouter {
    
    @Path("/static/*")
    @Method(Method.GET)
    public void serveStatic(RoutingContext ctx) {
        // 自定义静态资源处理
        String file = ctx.request().path().substring("/static/".length());
        ctx.response().sendFile("webroot/" + file);
    }
}
```

#### 内容协商

##### 支持的内容类型
```java
@Path("/user/:id")
@Method(Method.GET)
public Future<JsonObject> getUser(RoutingContext ctx, @PathParam("id") String userId) {
    String accept = ctx.request().getHeader("Accept");
    if (accept != null && accept.contains("application/xml")) {
        // 返回XML格式
        return sendBiz("biz://user.getById", new JsonObject().put("userId", userId))
            .map(user -> convertToXml(user));
    }
    // 默认返回JSON格式
    return sendBiz("biz://user.getById", new JsonObject().put("userId", userId));
}
```

#### 跨域处理

##### 配置CORS过滤器
```yaml
filters.http:
  24:
    pattern: /*
    class: io.vertx.ext.web.handler.CorsHandler
```

##### 自定义CORS配置
```java
public class CorsFilter implements Handler<RoutingContext> {
    
    @Override
    public void handle(RoutingContext ctx) {
        ctx.response()
            .putHeader("Access-Control-Allow-Origin", "*")
            .putHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
            .putHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        
        if ("OPTIONS".equals(ctx.request().method().name())) {
            ctx.response().setStatusCode(204).end();
        } else {
            ctx.next();
        }
    }
    
    public static CorsFilter create() {
        return new CorsFilter();
    }
}
```

#### 文件上传

##### 处理文件上传
```java
@Path("/upload")
@Method(Method.POST)
public Future<JsonObject> uploadFile(RoutingContext ctx) {
    Set<FileUpload> uploads = ctx.fileUploads();
    List<String> fileNames = new ArrayList<>();
    
    for (FileUpload upload : uploads) {
        String fileName = upload.fileName();
        String uploadedFileName = upload.uploadedFileName();
        // 处理文件保存逻辑
        fileNames.add(fileName);
    }
    
    return Future.succeededFuture(new JsonObject()
        .put("uploadedFiles", fileNames)
        .put("count", fileNames.size()));
}
```

#### 响应处理

##### 标准响应格式
```java
@Path("/api/response")
@Method(Method.GET)
public Future<JsonObject> standardResponse(RoutingContext ctx) {
    return sendBiz("biz://user.list", new JsonObject())
        .map(result -> new JsonObject()
            .put("code", 200)
            .put("message", "success")
            .put("data", result));
}
```

#### 完整示例

##### 完整的用户管理API
```java
@Http("/api/v1/users")
public class UserManagementRouter extends AbstractRouter {
    
    @Path("/")
    @Method(Method.GET)
    public Future<JsonObject> listUsers(RoutingContext ctx,
                                      @QueryParam("page") Integer page,
                                      @QueryParam("size") Integer size,
                                      @QueryParam("keyword") String keyword) {
        JsonObject params = new JsonObject()
            .put("pageNum", page != null ? page : 1)
            .put("pageSize", size != null ? size : 10)
            .put("keyword", keyword);
        return sendBiz("biz://user.page", params);
    }
    
    @Path("/:id")
    @Method(Method.GET)
    public Future<JsonObject> getUser(RoutingContext ctx, @PathParam("id") String userId) {
        return sendBiz("biz://user.getById", new JsonObject().put("userId", userId));
    }
    
    @Path("/")
    @Method(Method.POST)
    public Future<JsonObject> createUser(RoutingContext ctx, @Body User user) {
        return sendBiz("biz://user.create", JsonObject.mapFrom(user));
    }
    
    @Path("/:id")
    @Method(Method.PUT)
    public Future<JsonObject> updateUser(RoutingContext ctx,
                                       @PathParam("id") String userId,
                                       @Body User user) {
        JsonObject params = JsonObject.mapFrom(user).put("userId", userId);
        return sendBiz("biz://user.update", params);
    }
    
    @Path("/:id")
    @Method(Method.DELETE)
    public Future<JsonObject> deleteUser(RoutingContext ctx, @PathParam("id") String userId) {
        return sendBiz("biz://user.delete", new JsonObject().put("userId", userId));
    }
}
```

#### 测试和调试

##### 使用curl测试
```bash
# GET请求
curl -X GET http://localhost:8080/api/users

# POST请求
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"张三","email":"zhangsan@example.com"}'

# PUT请求
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"李四","email":"lisi@example.com"}'

# DELETE请求
curl -X DELETE http://localhost:8080/api/users/1
```

#### 性能优化

##### 连接配置
```yaml
application:
  context.path: /api
  http.port: 8080
  http.maxHeaderSize: 8192
  http.maxChunkSize: 8192
  http.maxInitialLineLength: 4096
  http.compressionSupported: true
```

#### 完整示例

参考测试代码获取完整使用示例：
- [测试用例](src/test/java/test/http/) - 完整HTTP服务示例
- [配置示例](src/test/resources/) - 各种配置示例
