# microservices-eureka-client

#### 项目介绍
tollge项目的Eureka客户端模块，提供服务注册和发现功能。该模块允许服务实例自动注册到Eureka服务器，并能够发现其他服务实例，实现微服务架构中的服务治理。

#### 核心特性
- **自动注册**：服务启动时自动注册到Eureka服务器
- **服务发现**：支持动态发现其他服务实例
- **健康检查**：支持服务健康状态上报
- **负载均衡**：支持客户端负载均衡
- **故障转移**：支持服务实例故障自动转移
- **配置管理**：支持动态配置更新

#### 依赖

需要Java 21及以上版本支持。
maven
```xml
<dependency>
    <groupId>com.tollge.modules</groupId>
    <artifactId>microservices-eureka-client</artifactId>
    <version>0.1.0</version>
</dependency>
```
Gradle
```groovy
implementation 'com.tollge.modules:microservices-eureka-client:0.1.0'
```

#### 快速开始

1. **添加依赖**
   在项目中引入microservices-eureka-client模块

2. **配置tollge.yml**
   ```yaml
   eureka:
     client:
       service-url:
         defaultZone: http://localhost:8761/eureka/
   ```

3. **启用Eureka客户端**
   在启动类上添加@EnableEurekaClient注解

#### 配置详解

##### 基本配置
```yaml
spring:
  application:
    name: user-service

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${server.port}
```

##### 高级配置
```yaml
spring:
  application:
    name: user-service

server:
  port: 8080

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    healthcheck:
      enabled: true
    lease:
      duration: 30
      
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${server.port}:${random.value}
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90
    health-check-url-path: /actuator/health
    status-page-url-path: /actuator/info
    metadata-map:
      zone: zone1
      profile: ${spring.profiles.active}
```

#### 服务注册配置

##### 基本服务注册
```java
@SpringBootApplication
@EnableEurekaClient
public class UserServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
    
    @RestController
    @RequestMapping("/users")
    public class UserController {
        
        @Value("${server.port}")
        private String port;
        
        @GetMapping("/{id}")
        public User getUser(@PathVariable Long id) {
            return new User(id, "用户-" + id + " from port: " + port);
        }
    }
}
```

##### 多实例配置
```yaml
# application-user1.yml
server:
  port: 8081

spring:
  application:
    name: user-service

eureka:
  instance:
    instance-id: user-service:8081

# application-user2.yml
server:
  port: 8082

spring:
  application:
    name: user-service

eureka:
  instance:
    instance-id: user-service:8082
```

#### 服务发现配置

##### 使用DiscoveryClient
```java
@Service
public class UserService {
    
    @Autowired
    private DiscoveryClient discoveryClient;
    
    @Autowired
    private RestTemplate restTemplate;
    
    public User getUser(Long id) {
        List<ServiceInstance> instances = 
            discoveryClient.getInstances("user-service");
        
        if (instances.isEmpty()) {
            throw new RuntimeException("No user service available");
        }
        
        ServiceInstance instance = instances.get(0);
        String url = instance.getUri() + "/users/" + id;
        
        return restTemplate.getForObject(url, User.class);
    }
}
```

##### 使用LoadBalancerClient
```java
@Service
public class OrderService {
    
    @Autowired
    private LoadBalancerClient loadBalancer;
    
    @Autowired
    private RestTemplate restTemplate;
    
    public Order getOrderWithUser(Long orderId) {
        ServiceInstance instance = 
            loadBalancer.choose("user-service");
        
        String url = instance.getUri() + "/users/" + orderId;
        User user = restTemplate.getForObject(url, User.class);
        
        return new Order(orderId, "订单" + orderId, user);
    }
}
```

#### 负载均衡

##### Ribbon配置
```yaml
user-service:
  ribbon:
    listOfServers: http://localhost:8081,http://localhost:8082
    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RoundRobinRule
    ConnectTimeout: 3000
    ReadTimeout: 3000
    MaxAutoRetriesNextServer: 3
    MaxAutoRetries: 1
```

##### 自定义负载均衡规则
```java
@Configuration
public class RibbonConfig {
    
    @Bean
    public IRule ribbonRule() {
        return new RoundRobinRule();
    }
    
    @Bean
    public IPing ribbonPing() {
        return new PingUrl();
    }
}
```

#### Feign客户端

##### 使用Feign
```java
@FeignClient(name = "user-service")
public interface UserServiceClient {
    
    @GetMapping("/users/{id}")
    User getUser(@PathVariable Long id);
    
    @PostMapping("/users")
    User createUser(@RequestBody User user);
    
    @PutMapping("/users/{id}")
    User updateUser(@PathVariable Long id, @RequestBody User user);
    
    @DeleteMapping("/users/{id}")
    void deleteUser(@PathVariable Long id);
}

@Service
public class OrderService {
    
    @Autowired
    private UserServiceClient userClient;
    
    public Order createOrder(Order order) {
        User user = userClient.getUser(order.getUserId());
        order.setUser(user);
        return order;
    }
}
```

#### 健康检查

##### 自定义健康检查
```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        // 自定义健康检查逻辑
        boolean healthy = checkServiceHealth();
        
        if (healthy) {
            return Health.up()
                .withDetail("service", "user-service")
                .withDetail("status", "running")
                .build();
        } else {
            return Health.down()
                .withDetail("service", "user-service")
                .withDetail("status", "down")
                .build();
        }
    }
    
    private boolean checkServiceHealth() {
        // 实现健康检查逻辑
        return true;
    }
}
```

#### 配置管理

##### 动态配置
```yaml
spring:
  cloud:
    config:
      discovery:
        enabled: true
        service-id: config-server
      fail-fast: true
      retry:
        max-attempts: 6
        initial-interval: 1000
        multiplier: 1.1
        max-interval: 2000
```

#### 容错处理

##### Hystrix配置
```yaml
hystrix:
  command:
    default:
      execution:
        isolation:
          thread:
            timeoutInMilliseconds: 5000
      circuitBreaker:
        requestVolumeThreshold: 20
        errorThresholdPercentage: 50
        sleepWindowInMilliseconds: 5000
        forceOpen: false
        forceClosed: false
```

##### 使用Hystrix
```java
@Service
public class UserService {
    
    @HystrixCommand(fallbackMethod = "getUserFallback")
    public User getUser(Long id) {
        return restTemplate.getForObject(
            "http://user-service/users/" + id, User.class);
    }
    
    public User getUserFallback(Long id) {
        return new User(id, "Fallback User");
    }
}
```

#### 监控和指标

##### 监控配置
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,hystrix.stream
  endpoint:
    health:
      show-details: always
```

##### 自定义指标
```java
@Component
public class ServiceMetrics {
    
    private final MeterRegistry meterRegistry;
    
    @Autowired
    public ServiceMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    public void recordServiceCall(String serviceName, boolean success) {
        meterRegistry.counter("service.calls", 
            "service", serviceName,
            "status", success ? "success" : "failure").increment();
    }
}
```

#### 多环境配置

##### 开发环境
```yaml
spring:
  profiles:
    active: dev

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

##### 测试环境
```yaml
spring:
  profiles:
    active: test

eureka:
  client:
    service-url:
      defaultZone: http://test-eureka:8761/eureka/
```

##### 生产环境
```yaml
spring:
  profiles:
    active: prod

eureka:
  client:
    service-url:
      defaultZone: http://eureka-server-1:8761/eureka/,http://eureka-server-2:8762/eureka/
```

#### 安全配置

##### 安全认证
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://username:password@localhost:8761/eureka/
```

#### 性能调优

##### 连接池配置
```yaml
eureka:
  client:
    eureka-service-url-poll-interval-seconds: 300
    eureka-connection-idle-timeout-seconds: 30
    eureka-server-read-timeout-seconds: 8
    eureka-server-connect-timeout-seconds: 5
```

#### 完整示例

##### 启动类
```java
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@EnableCircuitBreaker
public class UserServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
    
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

##### 服务配置
```java
@RestController
@RequestMapping("/users")
public class UserController {
    
    @Value("${server.port}")
    private String port;
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return new User(id, "用户-" + id + " from port: " + port);
    }
    
    @GetMapping("/health")
    public String health() {
        return "UP";
    }
}
```

#### 访问Eureka控制台

启动后访问：
- Eureka控制台: http://localhost:8761
- 服务健康检查: http://localhost:8080/actuator/health
- 服务指标: http://localhost:8080/actuator/metrics

#### 完整示例

参考测试代码获取完整使用示例：
- [测试用例](src/test/java/test/eureka/) - 完整Eureka客户端示例
- [配置示例](src/test/resources/) - 各种配置示例