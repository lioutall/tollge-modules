package com.tollge.modules.auth.common;

import com.google.common.collect.Sets;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Set;

/**
 * io.vertx.ext.auth.User接口实现
 */
public class AuthUser extends AbstractUser {

    private volatile JsonObject principal;

    @Override
    protected void doIsPermitted(String permission, Handler<AsyncResult<Boolean>> resultHandler) {
        Buffer buffer = Buffer.buffer();
        writeToBuffer(buffer);

        Set<String> cachedPermissions = Sets.newHashSet();
        readStringSet(buffer, cachedPermissions);

        boolean access = cachedPermissions.stream().anyMatch(per -> this.match(permission,per));
        resultHandler.handle(Future.succeededFuture(access));
    }

    private boolean match(String request, String cached){
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
        cachePermission(permission);
    }

    public void  appendPermissions(Collection<String> permissions){
        permissions.forEach(this::cachePermission);
    }

    private void readStringSet(Buffer buffer, Set<String> set) {
        int pos = 0;
        int num = buffer.getInt(pos);
        pos += 4;
        for (int i = 0; i < num; i++) {
            int len = buffer.getInt(pos);
            pos += 4;
            byte[] bytes = buffer.getBytes(pos, pos + len);
            pos += len;
            set.add(new String(bytes, StandardCharsets.UTF_8));
        }
    }
}
