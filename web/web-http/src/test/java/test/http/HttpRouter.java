package test.http;

import com.tollge.common.annotation.Method;
import com.tollge.common.annotation.mark.Path;
import com.tollge.common.annotation.mark.request.PathParam;
import com.tollge.common.verticle.AbstractRouter;
import com.tollge.modules.web.http.Http;
import io.vertx.core.AsyncResult;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Http("/web")
public class HttpRouter extends AbstractRouter {

    @Path(value = "/test/:key", method = Method.GET)
    public AsyncResult<JsonObject> one(RoutingContext rct, @PathParam("key") String key) {
        return sendBiz("biz://tt/one", key);
    }
}
