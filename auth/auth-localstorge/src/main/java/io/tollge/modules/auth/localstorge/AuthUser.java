package io.tollge.modules.auth.localstorge;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;

import java.util.Collection;

/**
 * io.vertx.ext.auth.User接口实现
 */
public class AuthUser extends AbstractUser {

    private volatile JsonObject principal;

    @Override
    protected void doIsPermitted(String permission, Handler<AsyncResult<Boolean>> resultHandler) {
        boolean access = this.cachedPermissions.stream().anyMatch(per -> this.match(permission,per));
        resultHandler.handle(Future.succeededFuture(access));
    }

    public boolean match(String request, String cached){
        return request.matches(cached.replaceAll(":[^/]+", "[^/]*"));
    }

    @Override
    public JsonObject principal() {
        return this.principal;
    }

    @Override
    public void setAuthProvider(AuthProvider authProvider) { }

    public void setPrincipal(JsonObject principal){
        this.principal = principal;
    }

    public void  appendPermission(String permission){
        this.cachedPermissions.add(permission);
    }

    public void  appendPermissions(Collection<String> permissions){
        this.cachedPermissions.addAll(permissions);
    }
}
