# auth-common

#### 项目介绍
tollge项目的认证鉴权模块，为Web应用提供完整的用户认证和权限管理功能。该模块作为Web模块的过滤器集成，基于token的认证机制，支持灵活的权限配置和自定义认证逻辑。

#### 核心特性
- **Token认证**：基于HTTP Header或Query参数的token认证
- **权限控制**：细粒度的URL和方法级别权限控制
- **会话管理**：支持本地缓存和分布式会话存储
- **匿名访问**：可配置的匿名访问URL模式
- **用户踢出**：支持强制用户下线功能
- **自动清理**：定时清理过期会话

#### 依赖

需要Java 21及以上版本支持。
maven
```xml
<dependency>
    <groupId>com.tollge.modules</groupId>
    <artifactId>auth-common</artifactId>
    <version>0.1.0</version>
</dependency>
```
Gradle
```groovy
implementation 'com.tollge.modules:auth-common:0.1.0'
```

#### 快速开始

1. **添加依赖**
   在项目中引入auth-common模块

2. **配置tollge.yml**
   ```yaml
   auth:
     impl: com.yourcompany.auth.YourAuthImpl
   ```

3. **实现自定义认证类**
   继承`AbstractAuth`类并实现必要方法

#### 配置说明

模块会自动加载以下配置：
```yaml
# tollge-auth-common.yml (模块内置配置)
filters.http:
  15:  # 过滤器优先级，数字越小优先级越高
    pattern: /*  # 拦截所有请求
    class: com.tollge.modules.auth.common.AuthHandler
```

#### 认证流程

1. **请求拦截**：所有HTTP请求都会被AuthHandler拦截
2. **Token获取**：从HTTP Header或Query参数获取token
3. **用户验证**：根据token获取用户登录信息
4. **权限检查**：检查用户是否有访问权限
5. **请求放行**：验证通过后继续处理请求

#### 自定义认证实现

创建自定义认证类需要继承`AbstractAuth`：

```java
public class YourAuthImpl extends AbstractAuth {
    
    @Override
    public void cacheLoginUser(String key, LoginUser loginUser, 
                               Handler<AsyncResult<Boolean>> resultHandler) {
        // 缓存用户登录信息
    }
    
    @Override
    public void getLoginUser(String key, Handler<AsyncResult<LoginUser>> resultHandler) {
        // 根据token获取用户信息
    }
    
    @Override
    public void getAnnoPermissions(Handler<AsyncResult<Set<String>>> resultHandler) {
        // 配置匿名访问的URL模式
        Set<String> annoUrls = ImmutableSet.of(
            "GET:/api/login",
            "POST:/api/register",
            "GET:/static/*"
        );
        resultHandler.handle(Future.succeededFuture(annoUrls));
    }
    
    @Override
    public void checkPermission(String permission, RoutingContext ctx, 
                               Handler<AsyncResult<Boolean>> handler) {
        // 检查用户权限
        // permission格式: "METHOD:/path" 如 "GET:/api/user/123"
    }
}
```

#### 匿名访问配置

通过重写`getAnnoPermissions`方法配置匿名访问：

```java
@Override
public void getAnnoPermissions(Handler<AsyncResult<Set<String>>> resultHandler) {
    Set<String> anonymousUrls = ImmutableSet.of(
        "GET:/api/public/*",      // 允许所有GET请求的/public/路径
        "POST:/api/login",        // 允许登录接口
        "*:/health"               // 允许所有方法的health检查
    );
    resultHandler.handle(Future.succeededFuture(anonymousUrls));
}
```

#### Token传递方式

支持两种token传递方式：

1. **HTTP Header**
   ```
   Authentication: your-token-here
   ```

2. **Query参数**
   ```
   GET /api/user/profile?token=your-token-here
   ```

#### 用户对象

认证成功后，用户信息会存储在`LoginUser`对象中：

```java
LoginUser loginUser = ctx.get("loginUser");
if (loginUser != null) {
    String userId = loginUser.getUserId();
    String nickname = loginUser.getNickname();
    List<Integer> roles = loginUser.getRoleIdList();
}
```

#### 权限字符串格式

权限检查使用以下格式：
- `GET:/api/user/123` - GET方法访问特定用户
- `POST:/api/user` - POST方法创建用户
- `PUT:/api/user/123` - PUT方法更新用户
- `DELETE:/api/user/123` - DELETE方法删除用户

#### 会话管理

支持以下会话管理功能：

1. **本地缓存**：使用内存Map存储会话
2. **分布式缓存**：可集成Redis等分布式缓存
3. **过期清理**：定时清理过期会话
4. **强制下线**：支持踢出特定用户

#### 完整示例

参考测试代码中的完整实现：
- [AuthCustom.java](src/test/java/test/auth/AuthCustom.java) - 本地缓存实现示例
- [测试用例](src/test/java/test/auth/) - 完整测试代码

#### 注意事项

1. 如果不配置`auth.impl`，将使用`AuthDefault`实现（所有请求都放行）
2. 过滤器优先级设置为15，可以根据需要调整
3. 建议在生产环境中使用分布式缓存存储会话信息
4. 匿名URL配置支持通配符`*`匹配
