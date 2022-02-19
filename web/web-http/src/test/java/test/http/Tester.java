package test.http;

import com.tollge.MainVerticle;
import com.tollge.common.util.Const;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
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
            Future<HttpClientRequest> req = client.request(HttpMethod.GET, 8090, "localhost", "/web/test/testkey");
            req.onFailure(err -> context.fail(err.getMessage()));
            req.onComplete(ar1 -> {
                if (ar1.succeeded()) {
                    HttpClientRequest request = ar1.result();

                    // 发送请求并处理响应
                    request.send("Hello World", ar -> {
                        if (ar.succeeded()) {
                            HttpClientResponse resp = ar.result();
                            resp.handler(b -> System.out.println(b.toString()));
                            context.assertEquals(200, resp.statusCode());
                        } else {
                            System.out.println("Something went wrong " + ar.cause().getMessage());
                        }
                    });
                }
                async.complete();
            });
        });

        suite.run(options).await(100000);

    }
}
