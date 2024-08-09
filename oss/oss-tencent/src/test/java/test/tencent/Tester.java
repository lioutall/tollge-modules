package test.tencent;

import com.tollge.MainVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestOptions;
import io.vertx.ext.unit.TestSuite;
import io.vertx.ext.unit.report.ReportOptions;
import org.junit.Test;

import java.time.LocalDateTime;

/**
 * @author toyer
 * @date 2018-10-23
 */
public class Tester {
    private Vertx vertx;

    @Test
    public void run() {
        TestOptions options = new TestOptions().addReporter(new ReportOptions().setTo("console"));
        TestSuite suite = TestSuite.create("test - redis");
        suite.before(ts -> {
            vertx = Vertx.vertx();
            vertx.deployVerticle(new MainVerticle(), ts.asyncAssertSuccess());
        });
        suite.after(ts -> {
            System.out.println("server close:"+ LocalDateTime.now());
            vertx.close(ts.asyncAssertSuccess());
        });

        suite.test("testOss", context -> {
            Async async = context.async();
            vertx.eventBus().request("biz://tt/testOss", new JsonObject().put("a", "bbb"), reply -> {
                if(reply.succeeded()) {
                    System.out.println("final: "+reply.result().body());
                } else {
                    System.err.println(reply.cause().getMessage());
                }
                //async.complete();
            });
        });

        suite.run(options).await(100000);
    }
}
