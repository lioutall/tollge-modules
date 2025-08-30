package com.tollge.modules.web.http;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyRouter {
    private MyRouter() {
    }

    private enum Singleton {
        // 单例
        INSTANCE;

        private MyRouter single;

        private Singleton() {
            single = new MyRouter();
        }

        public MyRouter getInstance() {
            return single;
        }
    }

    private Router router;

  public static Router getOrCreate(Vertx vertx, int port) {
    if (Singleton.INSTANCE.getInstance().router == null) {
      Singleton.INSTANCE.getInstance().router = Router.router(vertx);
      log.info("http服务监听端口:{}", port);
      MyHttpServer.getOrCreate(vertx).requestHandler(Singleton.INSTANCE.getInstance().router ).listen(port);
    }
    return Singleton.INSTANCE.getInstance().router;
  }
}
