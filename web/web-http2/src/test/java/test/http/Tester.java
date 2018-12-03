package test.http;

import com.tollge.MainVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestOptions;
import io.vertx.ext.unit.TestSuite;
import io.vertx.ext.unit.report.ReportOptions;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
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
        System.setProperty("vertx.cwd", "test/http");
        TestOptions options = new TestOptions().addReporter(new ReportOptions().setTo("console"));
        TestSuite suite = TestSuite.create("test - http2");
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
            WebClient client = WebClient.create(vertx);
            client.get(8443, "localhost", "/web/test/testkey")
                    .ssl(true)
                    .send(
                        b -> {
                            if(b.succeeded()) {
                                System.out.println(b.result().bodyAsString());
                                context.assertEquals(200, b.result().statusCode());
                                async.complete();
                            } else {
                                context.fail(b.cause().getMessage());
                            }
                        }
                    );
        });

        suite.run(options).await(100000);

    }
}
