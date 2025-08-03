# microservices-apollo

#### 项目介绍
tollge项目的Apollo配置中心模块，基于携程Apollo实现的分布式配置管理解决方案。该模块提供集中化的配置管理、实时配置推送、配置版本控制等功能，为微服务架构提供强大的配置管理能力。

#### 核心特性
- **集中配置管理**：支持集中化的配置管理
- **实时配置推送**：支持配置的实时推送和更新
- **配置版本控制**：支持配置版本管理和回滚
- **灰度发布**：支持配置的灰度发布
- **权限控制**：支持细粒度的配置权限管理
- **多环境支持**：支持开发、测试、生产等多环境配置
- **配置审计**：支持配置变更的完整审计

#### 依赖

需要Java 21及以上版本支持。
maven
```xml
<dependency>
    <groupId>com.tollge.modules</groupId>
    <artifactId>microservices-apollo</artifactId>
    <version>0.1.0</version>
</dependency>
```
Gradle
```groovy
implementation 'com.tollge.modules:microservices-apollo:0.1.0'
```

#### 快速开始

1. **添加依赖**
   在项目中引入microservices-apollo模块

2. **配置tollge.yml**
   ```yaml
   apollo:
     meta: http://localhost:8080
     app:
       id: your-app-id
     bootstrap:
       enabled: true
       namespaces: application
   ```

3. **配置参数说明**
   - `meta`: Apollo配置中心地址
   - `app.id`: 应用ID
   - `bootstrap.enabled`: 是否启用Apollo配置
   - `bootstrap.namespaces`: 配置命名空间

#### 配置详解

##### 基本配置
```yaml
apollo:
  meta: http://localhost:8080
  app:
    id: user-service
  bootstrap:
    enabled: true
    namespaces: application
```

##### 多环境配置
```yaml
# application-dev.yml
apollo:
  meta: http://dev-apollo:8080
  app:
    id: user-service
  bootstrap:
    enabled: true
    namespaces: application,database,redis

# application-test.yml
apollo:
  meta: http://test-apollo:8080
  app:
    id: user-service
  bootstrap:
    enabled: true
    namespaces: application,database,redis

# application-prod.yml
apollo:
  meta: http://prod-apollo:8080
  app:
    id: user-service
  bootstrap:
    enabled: true
    namespaces: application,database,redis
```

##### 高级配置
```yaml
apollo:
  meta: http://localhost:8080
  app:
    id: user-service
    cluster: default
  bootstrap:
    enabled: true
    eagerLoad:
      enabled: true
    namespaces: application,database,redis,log
  cacheDir: /opt/data/apollo-cache
  properties:
    order: 1
```

#### 配置管理

##### 1. 基本配置使用

###### 配置类
```java
@Component
@ConfigurationProperties(prefix = "user")
@RefreshScope
public class UserProperties {
    
    private String name;
    private String version;
    private int maxUsers;
    private boolean enableCache;
    
    // getters and setters
}
```

###### 使用配置
```java
@Service
public class UserService {
    
    @Value("${user.name}")
    private String userName;
    
    @Value("${user.max-users:1000}")
    private int maxUsers;
    
    @Autowired
    private UserProperties userProperties;
    
    public String getUserInfo() {
        return "Service: " + userProperties.getName() + 
               ", Version: " + userProperties.getVersion() +
               ", Max Users: " + userProperties.getMaxUsers();
    }
}
```

##### 2. 动态配置更新

###### 监听配置变更
```java
@Component
public class ApolloConfigChangeListener {
    
    @ApolloConfigChangeListener("application")
    public void onChange(ConfigChangeEvent changeEvent) {
        for (String key : changeEvent.changedKeys()) {
            ConfigChange change = changeEvent.getChange(key);
            System.out.println("配置变更: " + key + 
                             ", 旧值: " + change.getOldValue() + 
                             ", 新值: " + change.getNewValue());
        }
    }
}
```

###### 手动刷新配置
```java
@RestController
@RequestMapping("/config")
public class ConfigController {
    
    @Autowired
    private ApolloConfigManager configManager;
    
    @PostMapping("/refresh")
    public String refreshConfig() {
        configManager.refresh();
        return "配置已刷新";
    }
}
```

##### 3. 多命名空间配置

###### 配置多个命名空间
```yaml
apollo:
  bootstrap:
    enabled: true
    namespaces: application,database,redis,log
```

###### 使用不同命名空间
```java
@Component
public class DatabaseConfig {
    
    @ApolloConfig("database")
    private Config databaseConfig;
    
    @ApolloConfig("redis")
    private Config redisConfig;
    
    public String getDatabaseUrl() {
        return databaseConfig.getProperty("db.url", "localhost:3306");
    }
    
    public String getRedisHost() {
        return redisConfig.getProperty("redis.host", "localhost");
    }
}
```

#### 配置发布

##### 1. 灰度发布

###### 配置灰度规则
```java
@Component
public class GrayReleaseConfig {
    
    @ApolloConfig("application")
    private Config config;
    
    public boolean isFeatureEnabled(String userId) {
        String grayUsers = config.getProperty("feature.gray.users", "");
        List<String> grayUserList = Arrays.asList(grayUsers.split(","));
        return grayUserList.contains(userId);
    }
}
```

##### 2. 配置版本管理

###### 获取配置历史
```java
@RestController
@RequestMapping("/config")
public class ConfigHistoryController {
    
    @Autowired
    private ApolloOpenApiClient apiClient;
    
    @GetMapping("/history")
    public List<ReleaseDTO> getConfigHistory(
            @RequestParam String appId,
            @RequestParam String namespace) {
        
        return apiClient.getNamespaceReleaseHistory(
            appId, "default", namespace, 0, 10);
    }
}
```

#### 权限管理

##### 1. 应用权限

###### 配置应用权限
```yaml
apollo:
  portal:
    url: http://localhost:8070
    username: admin
    password: admin
```

##### 2. 命名空间权限

###### 配置命名空间权限
```java
@Configuration
public class ApolloSecurityConfig {
    
    @Bean
    public ApolloOpenApiClient apolloOpenApiClient() {
        return ApolloOpenApiClient.newBuilder()
            .withPortalUrl("http://localhost:8070")
            .withToken("your-token")
            .build();
    }
}
```

#### 多环境配置

##### 1. 环境隔离

###### 开发环境
```yaml
apollo:
  meta: http://dev-apollo:8080
  app:
    id: user-service
  bootstrap:
    enabled: true
    namespaces: application,database,redis
```

###### 测试环境
```yaml
apollo:
  meta: http://test-apollo:8080
  app:
    id: user-service
  bootstrap:
    enabled: true
    namespaces: application,database,redis
```

###### 生产环境
```yaml
apollo:
  meta: http://prod-apollo:8080
  app:
    id: user-service
  bootstrap:
    enabled: true
    namespaces: application,database,redis
```

##### 2. 集群配置

###### 集群配置
```yaml
apollo:
  meta: http://localhost:8080
  app:
    id: user-service
    cluster: beijing
  bootstrap:
    enabled: true
    namespaces: application,database,redis
```

#### 配置加密

##### 1. 敏感信息加密

###### 加密配置
```java
@Component
public class EncryptConfig {
    
    @Value("${db.password}")
    private String encryptedPassword;
    
    public String getDecryptedPassword() {
        // 解密逻辑
        return decrypt(encryptedPassword);
    }
    
    private String decrypt(String encrypted) {
        // 实现解密逻辑
        return encrypted;
    }
}
```

#### 配置审计

##### 1. 配置变更记录

###### 审计日志
```java
@Component
public class ConfigAuditLogger {
    
    @ApolloConfigChangeListener
    public void onChange(ConfigChangeEvent changeEvent) {
        for (String key : changeEvent.changedKeys()) {
            ConfigChange change = changeEvent.getChange(key);
            logAuditLog(key, change.getOldValue(), change.getNewValue());
        }
    }
    
    private void logAuditLog(String key, String oldValue, String newValue) {
        // 记录审计日志
        System.out.println("配置变更审计: " + key + 
                         " 从 " + oldValue + " 变更为 " + newValue);
    }
}
```

#### 监控和告警

##### 1. 配置监控

###### 配置监控指标
```java
@Component
public class ConfigMonitor {
    
    private final MeterRegistry meterRegistry;
    
    @Autowired
    public ConfigMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    @ApolloConfigChangeListener
    public void onChange(ConfigChangeEvent changeEvent) {
        meterRegistry.counter("config.changes", 
            "namespace", changeEvent.getNamespace()).increment(changeEvent.changedKeys().size());
    }
}
```

#### 完整示例

##### 配置管理类
```java
@Component
@ConfigurationProperties(prefix = "app")
@RefreshScope
public class AppConfig {
    
    private String name;
    private String version;
    private Database database;
    private Redis redis;
    
    @Data
    public static class Database {
        private String url;
        private String username;
        private String password;
        private int maxConnections;
    }
    
    @Data
    public static class Redis {
        private String host;
        private int port;
        private String password;
        private int timeout;
    }
}
```

##### 使用配置
```java
@Service
public class UserService {
    
    @Autowired
    private AppConfig appConfig;
    
    public String getServiceInfo() {
        return String.format("Service: %s v%s, DB: %s, Redis: %s:%d",
            appConfig.getName(),
            appConfig.getVersion(),
            appConfig.getDatabase().getUrl(),
            appConfig.getRedis().getHost(),
            appConfig.getRedis().getPort());
    }
}
```

#### 配置发布流程

##### 1. 配置创建
```java
@RestController
@RequestMapping("/config")
public class ConfigController {
    
    @Autowired
    private ApolloOpenApiClient apiClient;
    
    @PostMapping("/create")
    public String createConfig(@RequestBody ConfigRequest request) {
        OpenItemDTO item = new OpenItemDTO();
        item.setKey(request.getKey());
        item.setValue(request.getValue());
        item.setComment(request.getComment());
        
        apiClient.createOrUpdateItem(
            request.getAppId(), 
            request.getEnv(), 
            request.getCluster(), 
            request.getNamespace(), 
            item);
        
        return "配置创建成功";
    }
}
```

#### 配置回滚

##### 1. 配置版本回滚
```java
@RestController
@RequestMapping("/config")
public class ConfigRollbackController {
    
    @Autowired
    private ApolloOpenApiClient apiClient;
    
    @PostMapping("/rollback")
    public String rollbackConfig(@RequestBody RollbackRequest request) {
        apiClient.rollbackRelease(
            request.getAppId(),
            request.getEnv(),
            request.getCluster(),
            request.getNamespace(),
            request.getReleaseId());
        
        return "配置回滚成功";
    }
}
```

#### 配置验证

##### 1. 配置格式验证
```java
@Component
public class ConfigValidator {
    
    @ApolloConfigChangeListener
    public void validateConfig(ConfigChangeEvent changeEvent) {
        for (String key : changeEvent.changedKeys()) {
            ConfigChange change = changeEvent.getChange(key);
            if (!isValidConfig(key, change.getNewValue())) {
                throw new IllegalArgumentException("配置格式错误: " + key);
            }
        }
    }
    
    private boolean isValidConfig(String key, String value) {
        // 实现配置验证逻辑
        return true;
    }
}
```

#### 完整示例

##### 启动类
```java
@SpringBootApplication
@EnableApolloConfig
public class ApolloClientApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ApolloClientApplication.class, args);
    }
}
```

#### 访问Apollo控制台

启动后访问：
- Apollo控制台: http://localhost:8070
- 配置管理: http://localhost:8070/config.html

#### 完整示例

参考测试代码获取完整使用示例：
- [测试用例](src/test/java/test/apollo/) - 完整Apollo配置示例
- [配置示例](src/test/resources/) - 各种配置示例