package test.auth;

import com.google.common.collect.ImmutableSet;
import com.tollge.common.auth.AbstractAuth;
import com.tollge.common.auth.Subject;
import com.tollge.common.util.Const;
import com.tollge.modules.auth.common.AuthUser;
import io.netty.util.internal.StringUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class AuthCustom extends AbstractAuth {
    private Map<String, Subject> subjectCache = new HashMap<>();
    private static final String SESSION_HEADER_KEY = "Authentication";
    //默认sessionId 过期时间
    private static final long DEFAULT_SESSION_TIMEOUT = 1800L;

    @Override
    public void addSubject(String key, Subject subject, Handler<AsyncResult<String>> resultHandler) {
        subjectCache.put(key, subject);
        resultHandler.handle(Future.succeededFuture());
    }

    @Override
    public void getSubject(String key, Handler<AsyncResult<Subject>> resultHandler) {
        if (subjectCache == null) {
            resultHandler.handle(Future.failedFuture("unlogin"));
        } else {
            resultHandler.handle(Future.succeededFuture(subjectCache.get(key)));
        }
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
    public void login(RoutingContext ctx, JsonObject authInfo, Handler<AsyncResult<User>> resultHandler) {
        String username = authInfo.getString("username");
        String password = authInfo.getString("password");
        String openid = authInfo.getString("openid");
        if (StringUtil.isNullOrEmpty(username) && StringUtil.isNullOrEmpty(openid)) {
            resultHandler.handle(Future.failedFuture("authInfo must contain username in 'username' field"));
            return;
        }
        if (StringUtil.isNullOrEmpty(password) && StringUtil.isNullOrEmpty(openid)) {
            resultHandler.handle(Future.failedFuture("authInfo must contain password in 'password' field"));
            return;
        }

        AuthUser user = new AuthUser();
        user.setPrincipal(new JsonObject().put("username", username).put(Const.ID, 111));
        user.appendPermissions(ImmutableSet.of("GET:/web/userInfo"));
        resultHandler.handle(Future.succeededFuture(user));

    }

    @Override
    public void getAnnoPremissions(Handler<AsyncResult<ImmutableSet<String>>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(ImmutableSet.of("GET:/web/login")));
    }

    @Override
    public void kickUser(String key, Handler<AsyncResult<Boolean>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(true));
    }
}
