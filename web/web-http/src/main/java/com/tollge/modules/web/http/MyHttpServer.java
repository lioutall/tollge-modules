package com.tollge.modules.web.http;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyHttpServer {
    private MyHttpServer() {
    }

    private enum Singleton {
        // 单例
        INSTANCE;

        private MyHttpServer single;

        private Singleton() {
            single = new MyHttpServer();
        }

        public MyHttpServer getInstance() {
            return single;
        }
    }

    private HttpServer httpServer;

  public static HttpServer getOrCreate(Vertx vertx) {
    if (Singleton.INSTANCE.getInstance().httpServer == null) {
      Singleton.INSTANCE.getInstance().httpServer = vertx.createHttpServer();
    }
    return Singleton.INSTANCE.getInstance().httpServer;
  }
}
