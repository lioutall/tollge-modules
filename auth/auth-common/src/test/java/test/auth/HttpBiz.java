package test.auth;

import com.tollge.common.annotation.mark.Biz;
import com.tollge.common.annotation.mark.Path;
import com.tollge.common.util.LoginUtil;
import com.tollge.common.verticle.BizVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Biz("biz://tt")
public class HttpBiz extends BizVerticle {

    @Path("/userInfo")
    public void userInfo(Message<JsonObject> msg) {
        msg.reply(LoginUtil.getLoginUser(msg));
    }

}
