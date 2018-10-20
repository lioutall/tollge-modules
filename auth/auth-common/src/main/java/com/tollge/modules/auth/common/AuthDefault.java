package com.tollge.modules.auth.common;

import com.google.common.collect.ImmutableSet;
import com.tollge.common.auth.AbstractAuth;
import com.tollge.common.auth.Subject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

/**
 * 默认权限校验处理方法
 * @author toyer
 */
public class AuthDefault extends AbstractAuth {

    @Override
    public void addSubject(String key, Subject subject, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(Future.succeededFuture());
    }

    @Override
    public void getSubject(String key, Handler<AsyncResult<Subject>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(null));
    }

    @Override
    public void removeSubject(String key, Handler<AsyncResult<Void>> resultHandler) {
        resultHandler.handle(Future.succeededFuture());
    }

    @Override
    public boolean clearSubjects() {
        return false;
    }

    @Override
    public void sendtoBrowser(RoutingContext ctx, String sessionKey) {
        // nothing
    }

    @Override
    public String fetchFromBrowser(RoutingContext ctx) {
        return null;
    }

    @Override
    public void login(RoutingContext ctx, JsonObject authInfo, Handler<AsyncResult<User>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(new AuthUser()));
    }

    @Override
    public void getAnnoPremissions(Handler<AsyncResult<ImmutableSet<String>>> resultHandler) {
        // 默认所有URL都是匿名, 请重写该方法
        ImmutableSet<String> sendback = ImmutableSet.of("*");
        resultHandler.handle(Future.succeededFuture(sendback));
    }
}
