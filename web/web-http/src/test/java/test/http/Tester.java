package test.http;

import com.tollge.MainVerticle;
import com.tollge.common.util.Const;
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
        TestSuite suite = TestSuite.create("test - auth");
        suite.before(ts -> {
            vertx = Vertx.vertx();
            vertx.deployVerticle(new MainVerticle(), ts.asyncAssertSuccess());
        });
        suite.after(ts -> {
            System.out.println("server close:"+ LocalDateTime.now());
            vertx.close(ts.asyncAssertSuccess());
        });

        suite.test("web", context -> {
            Async async = context.async();
            HttpClient client = vertx.createHttpClient();
            HttpClientRequest req = client.get(8090, "localhost", "/web/test/testkey");
            req.exceptionHandler(err -> context.fail(err.getMessage()));
            req.handler(resp -> {
                resp.handler(b -> System.out.println(b.toString()));
                context.assertEquals(200, resp.statusCode());
                async.complete();
            });
            req.end();
        });

        suite.run(options).await(100000);

    }
}
