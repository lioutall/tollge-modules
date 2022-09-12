package test.http;

import com.tollge.common.annotation.Method;
import com.tollge.common.annotation.mark.Path;
import com.tollge.common.annotation.mark.request.Body;
import com.tollge.common.annotation.mark.request.PathParam;
import com.tollge.common.verticle.AbstractRouter;
import com.tollge.modules.web.http.Http;
import io.vertx.core.AsyncResult;
import io.vertx.ext.web.RoutingContext;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Http("/web")
public class HttpRouter extends AbstractRouter {

    @Path(value = "/test/:key", method = Method.GET, description = "method描述")
    public AsyncResult<User> one(RoutingContext rct, @PathParam(value = "key", description = "key描述") String key) {
        return sendBiz("biz://tt/one", key);
    }

    @Path(value = "/one", method = Method.POST, description = "method描述2")
    public AsyncResult<User> one(RoutingContext rct, @Body User user) {
        return sendBiz("biz://tt/one", user);
    }
}
