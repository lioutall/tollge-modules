package test.curd;

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

    @Path("/fetchOne")
    @NotNull(key="a", msg="a is test key, it can't be null")
    public void fetchOne(Message<JsonObject> msg) {
        this.one("testDB.fetchOne", msg,
                new JsonObject().put("a", msg.body().getString("a") + " response"));
    }

    @Path("/one")
    @NotNull(key="a", msg="a is test key, it can't be null")
    public void one(Message<JsonObject> msg) {
//        DaoVerticle dao = MyDao.getDao();
        this.one("testDB.one", msg,
                new JsonObject().put("id", msg.body().getLong("a")));
    }
}
