package com.tollge.modules.auth.common;

import com.google.common.collect.ImmutableSet;
import com.tollge.common.auth.AbstractAuth;
import com.tollge.common.auth.LoginUser;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.Set;

/**
 * 默认权限校验处理方法
 * @author toyer
 */
public class AuthDefault extends AbstractAuth {

    @Override
    public void cacheLoginUser(String key, LoginUser loginUser, Handler<AsyncResult<Boolean>> resultHandler) {
        resultHandler.handle(Future.succeededFuture());
    }

    @Override
    public void getLoginUser(String key, Handler<AsyncResult<LoginUser>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(null));
    }

    @Override
    public void removeLoginUser(String key, Handler<AsyncResult<Boolean>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(true));
    }


    @Override
    public boolean clearLoginUser() {
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
    public void getAnnoPermissions(Handler<AsyncResult<Set<String>>> resultHandler) {
        // 默认所有URL都是匿名, 请重写该方法
        ImmutableSet<String> sendback = ImmutableSet.of("*");
        resultHandler.handle(Future.succeededFuture(sendback));
    }

    @Override
    public void checkPermission(String permission, RoutingContext ctx, Handler<AsyncResult<Boolean>> handler) {
        handler.handle(Future.succeededFuture(true));
    }

    @Override
    public void kickLoginUser(String key, Handler<AsyncResult<Boolean>> resultHandler) {
        // nothing
        resultHandler.handle(Future.succeededFuture(true));
    }
}
