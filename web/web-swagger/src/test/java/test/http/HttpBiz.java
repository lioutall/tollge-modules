package test.http;

import com.tollge.common.annotation.mark.Biz;
import com.tollge.common.annotation.mark.Path;
import com.tollge.common.annotation.valid.NotNull;
import com.tollge.common.verticle.BizVerticle;
import io.vertx.core.eventbus.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
@Biz("biz://tt")
public class HttpBiz extends BizVerticle {
    /**
     * 测试
     */
    @Path("/one")
    @NotNull(key="key")
    public void one(Message<String> msg) {
        String key = msg.body();
        User user = new User();
        user.setName(key);
        user.setAge(22);
        user.setBornDay(new Date());
        msg.reply(user);
    }

}
