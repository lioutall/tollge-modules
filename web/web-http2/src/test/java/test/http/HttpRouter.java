package test.http;

import com.tollge.common.annotation.Method;
import com.tollge.common.annotation.mark.Path;
import com.tollge.common.verticle.AbstractRouter;
import com.tollge.modules.web.http2.Http2;
import io.vertx.ext.web.RoutingContext;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Http2("/web")
public class HttpRouter extends AbstractRouter {

    @Path(value = "/test/:key", method = Method.GET)
    public void one(RoutingContext rct) {
        sendBiz("biz://tt/one", rct);
    }
}
