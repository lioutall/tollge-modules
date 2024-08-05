package test.auth;

import com.tollge.common.ResultFormat;
import com.tollge.common.StatusCodeMsg;
import com.tollge.common.UFailureHandler;
import com.tollge.common.annotation.Method;
import com.tollge.common.annotation.mark.Path;
import com.tollge.common.auth.LoginUser;
import com.tollge.common.util.Const;
import com.tollge.common.verticle.AbstractRouter;
import com.tollge.modules.web.http.Http;
import io.vertx.ext.web.RoutingContext;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Http("/web")
public class HttpRouter extends AbstractRouter {

    @Path(value = "/login", method = Method.GET)
    public void login(RoutingContext rct) {
        AuthCustom auth = rct.get(Const.AUTH_CUSTOM);
        String key = "xxxxx";
        auth.sendtoBrowser(rct, key);
        auth.cacheLoginUser(key, new LoginUser(), reply -> {
            if (reply.succeeded()) {
                rct.response().end(ResultFormat.format(StatusCodeMsg.C200, reply.result()));
            } else {
                rct.response().end(UFailureHandler.commonFailure(reply.cause()));
            }
        });
    }

    @Path(value = "/userInfo", method = Method.GET)
    public void userInfo(RoutingContext rct) {
        sendBizWithUser(rct, "biz://tt/userInfo");
    }

}
