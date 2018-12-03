package test.http;

import com.tollge.common.annotation.Method;
import com.tollge.common.annotation.mark.Path;
import com.tollge.common.verticle.AbstractRouter;
import com.tollge.modules.web.http2.Http2;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Http2("/")
public class HttpRouter2 extends AbstractRouter {

    @Path(value = "", method = Method.GET)
    public void one(RoutingContext rct) {
        rct.request().params().add("key", "null");
        sendBiz("biz://tt/one", rct);
    }

    @Path(value = "a", method = Method.GET)
    public void a(RoutingContext rct) {
        rct.request().params().add("key", "a");
        sendBiz("biz://tt/one", rct);
    }
}
