package com.tollge.modules.auth.common;

import com.google.common.collect.ImmutableSet;
import com.tollge.common.ResultFormat;
import com.tollge.common.StatusCodeMsg;
import com.tollge.common.auth.AbstractAuth;
import com.tollge.common.auth.Subject;
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
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author toyer
 * @since 2018-04-10-14:58
 **/
@Slf4j
public class AuthHandlerImpl implements AuthHandler {
    private ImmutableSet<String> annoPermissionSet;
    private AbstractAuth authCustom;

    private String contextPath;

    public AuthHandlerImpl() {
        // 创建并注册权限处理器
        String implPath = Properties.getString("auth", "impl");
        if(StringUtil.isNullOrEmpty(implPath)) {
            this.authCustom = new AuthDefault();
        } else {
            try {
                Class cls = Class.forName(implPath);
                this.authCustom = (AbstractAuth)cls.getDeclaredConstructor().newInstance();
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                this.authCustom = new AuthDefault();
            }
        }

        this.contextPath = Properties.getString("application", "context.path");

        authCustom.getAnnoPremissions(f -> {
            if (f.succeeded()) {
                annoPermissionSet = f.result();
            }
        });

        if(authCustom.clearSubjects()) {
            // 一分钟清理一次
            MyVertx.vertx().setPeriodic(60_000, id -> authCustom.clearSubjects());
        }
    }

    @Override
    public void handle(RoutingContext ctx) {
        String sessionKey = authCustom.fetchFromBrowser(ctx);
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

        //获取当前用户
        getOrCreateSubject(sessionKey, f -> {
            if (f.succeeded()) {
                Subject subject = f.result();
                subject.storeCurrentSubject(ctx);

                if (checkAdmin(subject) || checkAnno(permission)) {
                    subject.refreshTime(LocalDateTime.now());
                    ctx.next();
                } else {
                    // 用户是否登录成功
                    if (subject.isAuthenticated()) {
                        subject.isAuthorised(permission, res -> {
                            // 用户是否具备访问permission权限
                            if (res.succeeded() && res.result()) {
                                subject.refreshTime(LocalDateTime.now());
                                ctx.next();
                            } else {
                                authCustom.failAuthenticate(ctx);
                            }
                        });
                    } else {
                        authCustom.failLogin(ctx);
                    }
                }
            } else {
                // 获取或创建不成功
                log.error("get or create subject failed", f.cause());
                ctx.response().end(ResultFormat.format(StatusCodeMsg.C316, f.cause()));
            }
        });

    }

    private boolean checkAdmin(Subject subject) {
        return subject != null && subject.getPrincipal() != null && "admin".equals(subject.getPrincipal().getString("role"));
    }

    private boolean checkAnno(String permission) {
        return this.annoPermissionSet.contains("*") || this.annoPermissionSet.contains(permission) ||
                this.annoPermissionSet.stream().anyMatch(per -> permission.startsWith(per.replaceAll("\\*", "")));
    }

    private void getOrCreateSubject(String sessionKey, Handler<AsyncResult<Subject>> resultHandler) {
        if (!StringUtil.isNullOrEmpty(sessionKey)) {
            authCustom.getSubject(sessionKey, f -> {
                if (f.succeeded() && f.result() != null) {
                    resultHandler.handle(Future.succeededFuture(f.result()));
                } else {
                    createSubject(sessionKey, resultHandler);
                }
            });
        } else {
            createSubject(sessionKey, resultHandler);
        }
    }

    private void createSubject(String sessionKey, Handler<AsyncResult<Subject>> resultHandler) {
        Subject newSubject = new Subject(sessionKey, authCustom);
        authCustom.addSubject(sessionKey, newSubject, a -> resultHandler.handle(Future.succeededFuture(newSubject)));
    }
}
