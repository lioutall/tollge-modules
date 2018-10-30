# auth-common

##### [中文版](https://github.com/lioutall/tollge-modules/blob/master/auth/auth-common/README_zh.md)
#### Introduction
A module of tollge specification.   
The extension server provides authentication. Depends on the [filter function](https://github.com/lioutall/tollge-modules/tree/master/web/web-http) provided by the network module.   
Insert the web as a filter into the handle sequence. Refer to the web filter configuration for details.

#### Dependency

Requires JDK1.8+.   
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

#### User Guide

1. Increase dependence
2. Modify tollge.yml
```
auth:
  impl: com.tollge.custom.AuthCustom
```
impl is a custom authentication implementation, please extends com.tollge.common.auth.AbstractAuth. All are passed while not configured.


3. Development is actually to inherit AbstractAuth, complement the implementation method. Here you can customize according to your system, whether it is local cache authentication, distributed authentication, or even authentication center.
There is a local cached authentication example in the demo.



