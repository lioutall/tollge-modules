package test.auth;

import com.tollge.MainVerticle;
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
            HttpClientRequest req = client.get(8090, "localhost", "/web/userInfo");
            req.exceptionHandler(err -> context.fail(err.getMessage()));
            req.handler(resp -> {
                resp.handler(b -> System.out.println(b.toString()));
                context.assertEquals(200, resp.statusCode());
                async.complete();
            });
            req.end();
        });

        suite.test("login", context -> {
            Async async = context.async();
            HttpClient client = vertx.createHttpClient();
            HttpClientRequest req = client.get(8090, "localhost", "/web/login");
            req.exceptionHandler(err -> context.fail(err.getMessage()));
            req.handler(resp -> {
                resp.headers().forEach( e ->{
                    System.out.println(e.getKey() + ":" + e.getValue());
                });
                resp.handler(b -> {
                    System.out.println("body:"+ b);
                });

                HttpClientRequest req2 = client.get(8090, "localhost", "/web/userInfo");
                req2.headers().addAll(resp.headers());
                req2.exceptionHandler(err -> context.fail(err.getMessage()));
                req2.handler(resp2 -> {
                    resp2.handler(b -> System.out.println("final:"+b));
                    context.assertEquals(200, resp2.statusCode());
                    async.complete();
                });
                req2.end();

            });
            req.end();
        });

        suite.run(options).await(100000);

    }
}
