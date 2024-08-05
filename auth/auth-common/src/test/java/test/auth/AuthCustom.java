package test.auth;

import com.google.common.collect.ImmutableSet;
import com.tollge.common.auth.AbstractAuth;
import com.tollge.common.auth.LoginUser;
import io.netty.util.internal.StringUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AuthCustom extends AbstractAuth {
    private final Map<String, LoginUser> subjectCache = new HashMap<>();
    private static final String SESSION_HEADER_KEY = "Authentication";
    //默认sessionId 过期时间
    private static final long DEFAULT_SESSION_TIMEOUT = 1800L;

    @Override
    public void cacheLoginUser(String key, LoginUser loginUser, Handler<AsyncResult<Boolean>> resultHandler) {
        subjectCache.put(key, loginUser);
        resultHandler.handle(Future.succeededFuture());
    }

    @Override
    public void getLoginUser(String key, Handler<AsyncResult<LoginUser>> resultHandler) {
        if (!subjectCache.containsKey(key)) {
            resultHandler.handle(Future.failedFuture("unlogin"));
        } else {
            resultHandler.handle(Future.succeededFuture(subjectCache.get(key)));
        }
    }

    @Override
    public void removeLoginUser(String key, Handler<AsyncResult<Boolean>> resultHandler) {
        subjectCache.remove(key);
        resultHandler.handle(Future.succeededFuture());
    }

    @Override
    public boolean clearLoginUser() {
        subjectCache.entrySet().removeIf(entry -> entry.getValue().getLoginTime().before(new Date(System.currentTimeMillis() - DEFAULT_SESSION_TIMEOUT*1000)));
        return true;
    }

    @Override
    public void sendtoBrowser(RoutingContext ctx, String sessionKey) {
        ctx.response().putHeader(SESSION_HEADER_KEY, sessionKey);
    }

    @Override
    public String fetchFromBrowser(RoutingContext ctx) {
        String token = ctx.request().getHeader(SESSION_HEADER_KEY);
        if (!StringUtil.isNullOrEmpty(token)) {
            return token;
        }

        String queryToken = ctx.queryParams().get("token");
        if (!StringUtil.isNullOrEmpty(queryToken)) {
            return queryToken;
        }
        return null;
    }

    @Override
    public void getAnnoPermissions(Handler<AsyncResult<Set<String>>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(ImmutableSet.of("GET:/web/login")));
    }

    @Override
    public void checkPermission(String s, RoutingContext ctx, Handler<AsyncResult<Boolean>> handler) {

    }

    @Override
    public void kickLoginUser(String key, Handler<AsyncResult<Boolean>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(true));
    }
}
