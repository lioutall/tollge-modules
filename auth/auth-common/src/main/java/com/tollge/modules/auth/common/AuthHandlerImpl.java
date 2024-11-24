package com.tollge.modules.auth.common;

import com.tollge.common.auth.AbstractAuth;
import com.tollge.common.auth.LoginUser;
import com.tollge.common.util.Const;
import com.tollge.common.util.MyVertx;
import com.tollge.common.util.Properties;
import io.netty.util.internal.StringUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.UUID;

/**
 * @author toyer
 * @since 2018-04-10-14:58
 **/
@Slf4j
public class AuthHandlerImpl implements AuthHandler {
    private Set<String> annoPermissionSet;
    private AbstractAuth authCustom;

    private String contextPath;

    public AuthHandlerImpl() {
        // 创建并注册权限处理器
        String implPath = Properties.getString("auth", "impl");
        if(StringUtil.isNullOrEmpty(implPath)) {
            this.authCustom = new AuthDefault();
        } else {
            try {
                Class<?> cls = Class.forName(implPath);
                this.authCustom = (AbstractAuth)cls.getDeclaredConstructor().newInstance();
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                log.error("auth handler impl is not exist " + implPath, e);
                this.authCustom = new AuthDefault();
            }
        }

        this.contextPath = Properties.getString("application", "context.path");

        authCustom.getAnnoPermissions(f -> {
            if (f.succeeded()) {
                annoPermissionSet = f.result();
            } else {
                log.error("get anno permission failed", f.cause());
            }
        });

        if(authCustom.clearLoginUser()) {
            // 一分钟清理一次
            MyVertx.vertx().setPeriodic(60_000, a -> authCustom.clearLoginUser());
        }
    }

    @Override
    public void handle(RoutingContext ctx) {
        String sessionKey = authCustom.fetchFromBrowser(ctx);
        ctx.put(Const.AUTH_CUSTOM, authCustom);
        if (StringUtil.isNullOrEmpty(sessionKey)) {
            sessionKey = UUID.randomUUID().toString();
            authCustom.sendtoBrowser(ctx, sessionKey);
        }

        HttpMethod method = ctx.request().method();

        //拼接权限字符串
        String path = ctx.request().path();
        if (!StringUtil.isNullOrEmpty(contextPath)) {
            path = path.replace(contextPath, "");
        }
        String permission = method+":"+path;

        if (checkAnno(permission)) {
            ctx.next();
            return;
        }

        //获取当前用户
        getLoginUser(sessionKey, f -> {
            if (f.succeeded()) {
                LoginUser loginUser = f.result();
                ctx.put(Const.LOGIN_USER, loginUser);

                if (checkAdmin(loginUser)) {
                    ctx.next();
                } else {
                    //权限校验
                    authCustom.checkPermission(permission, ctx, res -> {
                        // 用户是否具备访问permission权限
                        if (res.succeeded() && res.result()) {
                            ctx.next();
                        } else {
                            authCustom.failAuthenticate(ctx);
                        }
                    });
                }
            } else {
                // 未登录
                log.warn("get or create subject failed", f.cause());
                authCustom.failLogin(ctx);
            }
        });

    }

    private boolean checkAdmin(LoginUser user) {
        return user != null && user.getRoleIdList() != null && user.getRoleIdList().stream().anyMatch(o -> o == 1);
    }

    private boolean checkAnno(String permission) {
        return this.annoPermissionSet.contains("*") || this.annoPermissionSet.contains(permission) ||
                this.annoPermissionSet.stream().anyMatch(per -> permission.startsWith(per.replaceAll("\\*", "")));
    }

    private void getLoginUser(String sessionKey, Handler<AsyncResult<LoginUser>> resultHandler) {
        if (!StringUtil.isNullOrEmpty(sessionKey)) {
            authCustom.getLoginUser(sessionKey, f -> {
                if (f.succeeded() && f.result() != null) {
                    resultHandler.handle(Future.succeededFuture(f.result()));
                } else {
                    resultHandler.handle(Future.failedFuture("failed to get login user"));
                }
            });
        } else {
            resultHandler.handle(Future.failedFuture("failed to get login user"));
        }
    }
}
