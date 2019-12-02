package test.eureka;

import com.tollge.common.annotation.mark.Biz;
import com.tollge.common.annotation.mark.Path;
import com.tollge.common.annotation.valid.NotNull;
import com.tollge.common.verticle.BizVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Biz("biz://tt")
public class HttpBiz extends BizVerticle {
    /**
     * 测试
     */
    @Path("/one")
    @NotNull(key="key")
    public void one(Message<JsonObject> msg) {
        String key = msg.body().getString("key");
        msg.reply(key+" response");
    }

}
