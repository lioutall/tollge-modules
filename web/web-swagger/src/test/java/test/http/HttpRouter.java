package test.http;

import com.tollge.common.Page;
import com.tollge.common.annotation.Method;
import com.tollge.common.annotation.mark.Path;
import com.tollge.common.annotation.mark.request.*;
import com.tollge.common.verticle.AbstractRouter;
import com.tollge.modules.web.http.Http;
import io.vertx.core.AsyncResult;
import io.vertx.ext.web.RoutingContext;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.List;

@NoArgsConstructor
@Http("/web")
public class HttpRouter extends AbstractRouter {

    @Path(value = "/test/:key", method = Method.GET, description = "method描述")
    public AsyncResult<List<User>> one(RoutingContext rct, @PathParam(value = "key", description = "key描述") String key,
                                      @QueryParam(value="query", description = "查询条件1") String query,
                                      @FormParam(value = "formm", description = "查询条件2", required = true) String formm,
                                      @FileParam(value="query", description = "查询条件1") File file) {
        return sendBiz("biz://tt/one", key);
    }

    @Path(value = "/one", method = Method.POST, description = "method描述2")
    public AsyncResult<User> one(RoutingContext rct, @Body User user) {
        return sendBiz("biz://tt/one", user);
    }


  @Path(value = "/page", method = Method.POST, description = "method描述3")
  public AsyncResult<Page<User>> page(RoutingContext rct, @Body User user) {
    return sendBiz("biz://tt/one", user);
  }
}
