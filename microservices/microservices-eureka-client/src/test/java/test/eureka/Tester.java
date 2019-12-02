package test.eureka;

import com.tollge.MainVerticle;
import com.tollge.modules.microservices.eureka.EurekaClientUtil;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
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
        TestSuite suite = TestSuite.create("test - eurekaClient");
        suite.before(ts -> {
            vertx = Vertx.vertx();
            vertx.deployVerticle(new MainVerticle(), ts.asyncAssertSuccess());
        });
        suite.after(ts -> {
            System.out.println("server close:"+ LocalDateTime.now());
            vertx.close(ts.asyncAssertSuccess());
        });

        suite.test("eurekaClient", context -> {
            Async async = context.async();
            EurekaClientUtil.get("mshop-web-lt", "/web/test/clientCall", new JsonObject(), res->{
                System.out.println(res.result().body());
                async.complete();
            });
        });

        suite.run(options).await(100000);

    }
}
