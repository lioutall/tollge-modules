package test.redis;

import com.google.common.collect.Lists;
import com.tollge.common.annotation.mark.Biz;
import com.tollge.common.annotation.mark.Path;
import com.tollge.common.verticle.BizVerticle;
import com.tollge.modules.data.redis.MyRedis;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Biz("biz://tt")
public class HttpBiz extends BizVerticle {

    @Path("/testRedis")
    public void testRedis(Message<JsonObject> msg) {
        MyRedis.set("key", "value");

        MyRedis.get("key").onComplete(res ->{
            if (res.succeeded()) {
                System.out.println("test result ------ =" + res.result().toString());
                msg.reply(res.result().toString());
            } else {
                msg.reply("something error");
                res.cause().printStackTrace();
            }
        });

        MyRedis.getOrDefault("a", "dddddd").onComplete(r -> {
            System.out.println(r.result());
        });

        MyRedis.tryGetDistributedLock("lockKey", "123", 100000).onComplete(r -> {
            if (r.failed()) {
                r.cause().printStackTrace();
            }
            System.out.println("tryGetDistributedLock " + r.result());
        });

        MyRedis.tryGetDistributedLock("lockKey", "123", 100000).onComplete(r -> {
            if (r.failed()) {
                r.cause().printStackTrace();
            }
            System.out.println("tryGetDistributedLock " + r.result());
        });

        MyRedis.ttl("lockKey").onComplete(r->{
            System.out.println(r.result());
        });

        MyRedis.releaseDistributedLock("lockKey", "123").onComplete(r -> {
            if (r.failed()) {
                r.cause().printStackTrace();
            }
            System.out.println("releaseDistributedLock " + r.succeeded() + "," + r.result());
        });

       MyRedis.mget(Lists.newArrayList("key", "a", "lockKey")).onComplete(r -> {
           if (r.succeeded()) {
               Response result = r.result();
               System.out.println(result);
           }
       });

        for (int i = 0; i < 1000; i++) {
            MyRedis.incr("innn").onComplete(r -> {
                if (r.failed()) {
                    r.cause().printStackTrace();
                }
            });
        }

    }

}
