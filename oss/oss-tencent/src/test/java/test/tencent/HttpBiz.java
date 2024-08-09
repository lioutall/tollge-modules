package test.tencent;

import com.tollge.common.annotation.mark.Biz;
import com.tollge.common.annotation.mark.Path;
import com.tollge.common.verticle.BizVerticle;
import com.tollge.modules.oss.tencent.MyTencentOss;
import com.tollge.modules.oss.tencent.TmpSecret;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Biz("biz://tt")
public class HttpBiz extends BizVerticle {

    @Path("/testOss")
    public void testRedis(Message<JsonObject> msg) {
        TmpSecret tmpSecret = MyTencentOss.getTmpSecret("product-public-1251579316", 600);
        System.out.println(tmpSecret);

        msg.reply(Json.CODEC.toString(tmpSecret));
    }

}
