package com.tollge.modules.auth.common;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * 定制权限过滤器
 * @author toyer
 */
public interface AuthHandler extends Handler<RoutingContext> {

    /**
     * Create a auth handler
     *
     * @return the auth handler
     */
    static AuthHandler create() {
        return new AuthHandlerImpl();
    }

}
