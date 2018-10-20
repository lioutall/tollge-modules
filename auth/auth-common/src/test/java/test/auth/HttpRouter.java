package test.auth;

import com.tollge.common.annotation.Method;
import com.tollge.common.annotation.mark.Path;
import com.tollge.common.auth.Subject;
import com.tollge.modules.web.http.Http;
import com.tollge.common.verticle.AbstractRouter;
import io.vertx.ext.web.RoutingContext;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Http("/web")
public class HttpRouter extends AbstractRouter {

    @Path(value = "/login", method = Method.GET)
    public void login(RoutingContext rct) {
        Subject.getCurrentSubject(rct).login(rct, null, reply->{

        });
    }

    @Path(value = "/userInfo", method = Method.GET)
    public void one(RoutingContext rct) {
        sendBiz("biz://tt/userInfo", rct);
    }

}
