# microservices-eureka-server

#### 项目介绍
tollge项目的Eureka服务注册中心模块，基于Netflix Eureka实现的服务发现组件。该模块提供服务注册、发现、健康检查等功能，为微服务架构提供核心的服务治理能力。

#### 核心特性
- **服务注册**：支持服务实例自动注册
- **服务发现**：支持服务实例动态发现
- **健康检查**：支持服务健康状态监控
- **高可用**：支持集群部署，避免单点故障
- **负载均衡**：支持客户端负载均衡
- **RESTful API**：提供完整的RESTful API接口

#### 依赖

需要Java 21及以上版本支持。
maven
```xml
<dependency>
    <groupId>com.tollge.modules</groupId>
    <artifactId>microservices-eureka-server</artifactId>
    <version>0.1.0</version>
</dependency>
```
Gradle
```groovy
implementation 'com.tollge.modules:microservices-eureka-server:0.1.0'
```

#### 快速开始

1. **添加依赖**
   在项目中引入microservices-eureka-server模块

2. **配置tollge.yml**
   ```yaml
   eureka:
     server:
       port: 8761
       hostname: localhost
   ```

3. **启动服务**
   启动后访问 http://localhost:8761 查看Eureka控制台

#### 配置详解

##### 基本配置
```yaml
eureka:
  server:
    port: 8761
    hostname: localhost
```

##### 高可用配置
```yaml
eureka:
  server:
    port: 8761
    hostname: eureka-server-1
    
  client:
    serviceUrl:
      defaultZone: http://eureka-server-2:8762/eureka/,http://eureka-server-3:8763/eureka/
```

##### 高级配置
```yaml
eureka:
  server:
    port: 8761
    hostname: localhost
    
  server:
    enable-self-preservation: true
    renewal-percent-threshold: 0.85
    eviction-interval-timer-in-ms: 60000
    
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
```

#### 服务注册配置

##### 服务端配置
```yaml
server:
  port: 8761

spring:
  application:
    name: eureka-server

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
```

##### 集群配置
```yaml
# eureka-server-1.yml
server:
  port: 8761

eureka:
  instance:
    hostname: eureka-server-1
  client:
    service-url:
      defaultZone: http://eureka-server-2:8762/eureka/,http://eureka-server-3:8763/eureka/

# eureka-server-2.yml
server:
  port: 8762

eureka:
  instance:
    hostname: eureka-server-2
  client:
    service-url:
      defaultZone: http://eureka-server-1:8761/eureka/,http://eureka-server-3:8763/eureka/

# eureka-server-3.yml
server:
  port: 8763

eureka:
  instance:
    hostname: eureka-server-3
  client:
    service-url:
      defaultZone: http://eureka-server-1:8761/eureka/,http://eureka-server-2:8762/eureka/
```

#### 服务发现配置

##### 客户端配置
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

#### 健康检查

##### 服务端健康检查
```yaml
eureka:
  server:
    enable-self-preservation: true
    renewal-percent-threshold: 0.85
    eviction-interval-timer-in-ms: 60000
```

##### 客户端健康检查
```yaml
eureka:
  instance:
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90
    health-check-url-path: /actuator/health
    status-page-url-path: /actuator/info
```

#### 安全配置

##### 基本认证
```yaml
spring:
  security:
    user:
      name: admin
      password: password

eureka:
  server:
    enable-self-preservation: true
```

##### 访问控制
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
            .antMatchers("/eureka/**").permitAll()
            .anyRequest().authenticated()
            .and()
            .httpBasic();
    }
}
```

#### 监控和管理

##### 端点配置
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

##### 自定义健康检查
```java
@Component
public class EurekaHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        // 自定义健康检查逻辑
        return Health.up()
            .withDetail("eureka", "running")
            .build();
    }
}
```

#### 高可用部署

##### Docker部署
```yaml
# docker-compose.yml
version: '3.8'
services:
  eureka-server-1:
    image: tollge/eureka-server:latest
    ports:
      - "8761:8761"
    environment:
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server-2:8762/eureka/,http://eureka-server-3:8763/eureka/
      - EUREKA_INSTANCE_HOSTNAME=eureka-server-1

  eureka-server-2:
    image: tollge/eureka-server:latest
    ports:
      - "8762:8762"
    environment:
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server-1:8761/eureka/,http://eureka-server-3:8763/eureka/
      - EUREKA_INSTANCE_HOSTNAME=eureka-server-2

  eureka-server-3:
    image: tollge/eureka-server:latest
    ports:
      - "8763:8763"
    environment:
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server-1:8761/eureka/,http://eureka-server-2:8762/eureka/
      - EUREKA_INSTANCE_HOSTNAME=eureka-server-3
```

##### Kubernetes部署
```yaml
# eureka-deployment.yml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: eureka-server
spec:
  replicas: 3
  selector:
    matchLabels:
      app: eureka-server
  template:
    metadata:
      labels:
        app: eureka-server
    spec:
      containers:
      - name: eureka-server
        image: tollge/eureka-server:latest
        ports:
        - containerPort: 8761
        env:
        - name: EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE
          value: "http://eureka-server-1:8761/eureka/,http://eureka-server-2:8762/eureka/,http://eureka-server-3:8763/eureka/"
```

#### 服务注册示例

##### 服务提供者
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
        
        @GetMapping("/{id}")
        public User getUser(@PathVariable Long id) {
            // 返回用户信息
            return new User(id, "张三");
        }
    }
}
```

##### 服务消费者
```java
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
public class OrderServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
    
    @FeignClient(name = "user-service")
    public interface UserServiceClient {
        
        @GetMapping("/users/{id}")
        User getUser(@PathVariable Long id);
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
```

##### 自定义负载均衡
```java
@Configuration
public class RibbonConfig {
    
    @Bean
    public IRule ribbonRule() {
        return new RoundRobinRule();
    }
}
```

#### 故障转移

##### 熔断器配置
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
```

#### 监控和诊断

##### 服务状态监控
```java
@RestController
@RequestMapping("/eureka")
public class EurekaController {
    
    @Autowired
    private EurekaClient eurekaClient;
    
    @GetMapping("/services")
    public List<String> getServices() {
        return eurekaClient.getApplications().getRegisteredApplications()
            .stream()
            .map(Application::getName)
            .collect(Collectors.toList());
    }
    
    @GetMapping("/instances/{serviceName}")
    public List<InstanceInfo> getInstances(@PathVariable String serviceName) {
        return eurekaClient.getInstancesByVipAddress(serviceName, false);
    }
}
```

#### 日志配置

##### 日志级别
```yaml
logging:
  level:
    com.netflix.eureka: DEBUG
    com.netflix.discovery: DEBUG
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
@EnableEurekaServer
public class EurekaServerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

#### 访问控制台

启动后访问：
- Eureka控制台: http://localhost:8761
- 健康检查: http://localhost:8761/actuator/health
- 指标监控: http://localhost:8761/actuator/metrics

#### 完整示例

参考测试代码获取完整使用示例：
- [测试用例](src/test/java/test/eureka/) - 完整Eureka服务示例
- [配置示例](src/test/resources/) - 各种配置示例