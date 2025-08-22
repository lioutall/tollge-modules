package com.tollge.modules.web.statics;

import com.tollge.common.util.Properties;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StaticVerticle extends AbstractVerticle {

    @Override
    public void start() {
        Router router = Router.router(vertx);

        // 获取静态文件根目录配置
        String webRoot = Properties.getString("web.static", "web.root", "webroot");
        String uriPrefix = Properties.getString("web.static", "uri.prefix", "/static");

        // 创建静态文件处理器
        StaticHandler staticHandler = StaticHandler.create();
        staticHandler.setWebRoot(webRoot);
        staticHandler.setDefaultContentEncoding("UTF-8");

        // 挂载路由
        router.route(uriPrefix + "*").handler(staticHandler);

        int port = Properties.getInteger("application", "static.port");
        log.info("静态web服务监听端口:{}, 根目录:{}, URI前缀:{}", port, webRoot, uriPrefix);
        vertx.createHttpServer().requestHandler(router).listen(port);
    }
}
