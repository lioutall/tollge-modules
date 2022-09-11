package test.auth;

import com.tollge.common.ResultFormat;
import com.tollge.common.StatusCodeMsg;
import com.tollge.common.UFailureHandler;
import com.tollge.common.annotation.Method;
import com.tollge.common.annotation.mark.Path;
import com.tollge.common.auth.Subject;
import com.tollge.common.verticle.AbstractRouter;
import com.tollge.modules.web.http.Http;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Http("/web")
public class HttpRouter extends AbstractRouter {

    @Path(value = "/login", method = Method.GET)
    public void login(RoutingContext rct) {
        Subject.getCurrentSubject(rct).login(rct, new JsonObject().put("username", "1").put("password", "1"), reply -> {
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
