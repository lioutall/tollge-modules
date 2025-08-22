# web-static

基于tollge框架的静态web服务器模块。

## 简介

该模块提供了一个简单的静态文件web服务器，可以提供HTML、CSS、JavaScript、图片等静态文件服务。

## 配置

在 `tollge.yml` 中配置:

```yaml
web.static:
  web.root: webroot     # 静态文件根目录，默认为webroot
  uri.prefix: /         # URI前缀，默认为/
  
application:
  http.port: 8080       # HTTP端口
```

## 使用

将该模块添加到项目的依赖中即可启用。静态文件应放置在 `web.root` 指定的目录中。