package com.tollge.modules.web.http;

import  com.tollge.common.UFailureHandler;
import  com.tollge.common.util.Properties;
import  com.tollge.common.util.ReflectionUtil;
import  com.tollge.common.verticle.AbstractRouter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class HttpVerticle extends AbstractVerticle {

    @Override
    @SuppressWarnings("unchecked")
    public void start() {
        io.vertx.ext.web.Router router = io.vertx.ext.web.Router.router(vertx);

        // 过滤器初始化
        Map<String, Object> filters = Properties.getGroup("filters.http");
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
        Set<Class<?>> set = ReflectionUtil.getClassesWithAnnotated(Http.class);
        for (Class<?> c : set) {
            Http mark = c.getAnnotation(Http.class);
            try {
                AbstractRouter abstractRouter = (AbstractRouter)c.getDeclaredConstructor().newInstance();
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
                        case OPTIONS: r = router.options(contextPath);break;
                        default: r = router.route(contextPath);break;
                    }
                    r.produces(pathMark.contentType()).handler(routingContextConsumer)
                    .failureHandler(rct -> {
                        log.error("调用Biz[{}]失败", contextPath, rct.failure());
                        rct.response().end(UFailureHandler.commonFailure(rct.failure()));
                    });
                    log.info("监听 {}:{}", pathMark.method().name(), path);
                });
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                log.error("初始化({})失败", c, e);
            }

        }

        int port = Properties.getInteger("application", "http.port");
        log.info("http服务监听端口:{}", port);
        vertx.createHttpServer().requestHandler(router).listen(port);
    }

}
