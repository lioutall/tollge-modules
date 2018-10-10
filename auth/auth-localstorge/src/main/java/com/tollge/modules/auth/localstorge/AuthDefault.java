package com.tollge.modules.auth.localstorge;

import com.google.common.collect.ImmutableSet;
import  com.tollge.common.auth.AbstractAuth;
import  com.tollge.common.auth.Subject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认权限校验处理方法
 * @author toyer
 */
public class AuthDefault extends AbstractAuth {
    /**
     * 内存存储型
      */
    private Map<String, Subject> subjectCache = new ConcurrentHashMap<>();
    private static final String SESSION_HEADER_KEY = "x-access-token";
    //默认sessionId 过期时间
    private static final long DEFAULT_SESSION_TIMEOUT = 1800L;

    @Override
    public void addSubject(String key, Subject subject, Handler<AsyncResult<String>> resultHandler) {
        subjectCache.put(key, subject);
        resultHandler.handle(Future.succeededFuture());
    }

    @Override
    public void getSubject(String key, Handler<AsyncResult<Subject>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(subjectCache.get(key)));
    }

    @Override
    public void removeSubject(String key, Handler<AsyncResult<Void>> resultHandler) {
        subjectCache.remove(key);
        resultHandler.handle(Future.succeededFuture());
    }

    @Override
    public boolean clearSubjects() {
        LocalDateTime now = LocalDateTime.now();
        subjectCache.entrySet().removeIf(entry -> entry.getValue().getTime().isBefore(now.minusSeconds(DEFAULT_SESSION_TIMEOUT)));
        return true;
    }

    @Override
    public void sendtoBrowser(RoutingContext ctx, String sessionKey) {
        ctx.addCookie(Cookie.cookie(SESSION_HEADER_KEY, sessionKey));
    }

    @Override
    public String fetchFromBrowser(RoutingContext ctx) {
        if(ctx.getCookie(SESSION_HEADER_KEY) != null) {
            return ctx.getCookie(SESSION_HEADER_KEY).getValue();
        }
        return null;
    }

    @Override
    public void login(RoutingContext ctx, JsonObject authInfo, Handler<AsyncResult<User>> resultHandler) {
        String username = authInfo.getString("username");
        if (username == null) {
            resultHandler.handle(Future.failedFuture("authInfo must contain username in 'username' field"));
            return;
        }
        String password = authInfo.getString("password");
        if (password == null) {
            resultHandler.handle(Future.failedFuture("authInfo must contain password in 'password' field"));
            return;
        }

        AuthUser user = new AuthUser();
        resultHandler.handle(Future.succeededFuture(user));
    }

    @Override
    public void getAnnoPremissions(Handler<AsyncResult<ImmutableSet<String>>> resultHandler) {
        // 默认所有URL都是匿名, 请重写该方法
        ImmutableSet<String> sendback = ImmutableSet.of("*");
        resultHandler.handle(Future.succeededFuture(sendback));
    }
}
