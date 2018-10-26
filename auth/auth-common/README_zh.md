# auth-common

#### 项目介绍
tollge项目的模块
扩展server提供鉴权功能.依赖web模块提供的[filters功能]().
作为一个filter插入web的handle序列.具体参考web的filter配置说明.

#### 依赖

需要JDK1.8及以上版本支持.   
maven
```
<dependency>
    <groupId>com.tollge.modules</groupId>
    <artifactId>auth-common</artifactId>
    <version>0.1.0</version>
</dependency>
```
Gradle
```
compile 'com.tollge.modules:auth-common:0.1.0'
```

#### 用户指导

1. 增加依赖
2. 配置tollge.yml
```
auth:
  impl: com.tollge.custom.AuthCustom
```
impl 是自定义的鉴权实现, 请继承com.tollge.common.auth.AbstractAuth.  如果不配置则全部放通.


3. 开发其实就是继承AbstractAuth, 补充实现方法. 这里可以根据你的系统, 定制到底是本地缓存式鉴权, 还是分布式鉴权, 甚至鉴权中心.
demo里有一个本地缓存式鉴权例子.


