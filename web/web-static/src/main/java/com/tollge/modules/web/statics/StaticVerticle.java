package com.tollge.modules.web.statics;

import com.tollge.common.util.Properties;
import com.tollge.modules.web.http.MyRouter;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.handler.StaticHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StaticVerticle extends AbstractVerticle {

    @Override
    public void start() {
      int port = Properties.getInteger("application", "http.port");
      io.vertx.ext.web.Router router = MyRouter.getOrCreate(vertx, port);

        // 获取静态文件根目录配置
        String webRoot = Properties.getString("web.static", "web.root", "webroot");
        String uriPrefix = Properties.getString("web.static", "uri.prefix", "/static");

        // 创建静态文件处理器
        StaticHandler staticHandler = StaticHandler.create();
        staticHandler.setWebRoot(webRoot);
        staticHandler.setDefaultContentEncoding("UTF-8");

        // 挂载路由
        router.route(uriPrefix + "*").handler(staticHandler);
    }
}
