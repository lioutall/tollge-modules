package com.tollge.modules.web.http2;

import  com.tollge.common.UFailureHandler;
import  com.tollge.common.util.Properties;
import  com.tollge.common.util.ReflectionUtil;
import  com.tollge.common.verticle.AbstractRouter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class Http2Verticle extends AbstractVerticle {

    @Override
    public void start() {
        io.vertx.ext.web.Router router = io.vertx.ext.web.Router.router(vertx);

        // 过滤器初始化
        Map<String, Object> filters = Properties.getGroup("filters.http2");
        filters.entrySet().stream().collect(Collectors.groupingBy(e -> e.getKey().replaceAll("\\.\\S+", ""),
                Collectors.toMap(e -> e.getKey().replaceAll("^\\d+\\.", ""), Map.Entry::getValue)))
                .entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey)).forEach(c -> {
            try {
                JsonObject o = JsonObject.mapFrom(c.getValue());
                router.route(o.getString("pattern")).handler((Handler<RoutingContext>) Class.forName(o.getString("class")).getMethod("create").invoke(null));
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException | RuntimeException e) {
                log.error("加载过滤器[{}]失败", c.getValue(), e);
            }
        });

        // routers初始化
        Set<Class<?>> set = ReflectionUtil.getClassesWithAnnotated(Http2.class);
        for (Class<?> c : set) {
            Http2 mark = c.getAnnotation(Http2.class);
            try {
                AbstractRouter abstractRouter = (AbstractRouter)c.newInstance();
                abstractRouter.getMap().forEach((pathMark, routingContextConsumer) -> {
                    String path = mark.value().concat(pathMark.value());
                    String contextPath = Properties.getString("application","context.path", "") + path;
                    Route r = null;
                    switch (pathMark.method()) {
                        case ROUTE: r = router.route(contextPath);break;
                        case GET: r = router.get(contextPath);break;
                        case POST: r = router.post(contextPath);break;
                        case PUT: r = router.put(contextPath);break;
                        case DELETE: r = router.delete(contextPath);break;
                        case TRACE: r = router.trace(contextPath);break;
                        default: r = router.route(contextPath);break;
                    }
                    r.produces(pathMark.contentType()).handler(routingContextConsumer)
                    .failureHandler(rct -> {
                        log.error("调用Biz[{}]失败", contextPath, rct.failure());
                        rct.response().end(UFailureHandler.commonFailure(rct.failure()));
                    });
                    log.info("监听 {}:{}", pathMark.method().name(), path);
                });
            } catch (InstantiationException | IllegalAccessException e) {
                log.error("初始化({})失败", c, e);
            }

        }

        int port = Properties.getInteger("application", "http2.port");
        String keyPath = Properties.getString("web", "http2.keyPath");
        String certPath = Properties.getString("web", "http2.certPath");
        int timeoutSeconds = Properties.getInteger("web", "http2.timeoutSeconds");
        log.info("http2服务监听端口:{}", port);
        vertx.createHttpServer(
                new HttpServerOptions()
                        .setSsl(true)
                        .setUseAlpn(true)
                        .setPemKeyCertOptions(new PemKeyCertOptions().setKeyPath(keyPath).setCertPath(certPath))
                        .setIdleTimeout(timeoutSeconds)
        ).requestHandler(router::accept).listen(port);
    }

}
