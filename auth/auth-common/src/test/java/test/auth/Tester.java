package test.auth;

import com.tollge.MainVerticle;
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
            Future<HttpClientRequest> req = client.request(HttpMethod.GET, 8090, "localhost", "/web/userInfo");
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
                        async.complete();
                    });
                }

            });
        });

        suite.test("login", context -> {
            Async async = context.async();
            HttpClient client = vertx.createHttpClient();
            Future<HttpClientRequest> req = client.request(HttpMethod.GET, 8090, "localhost", "/web/login");
            req.onFailure(err -> context.fail(err.getMessage()));
            req.onComplete(ar1 -> {
                if (ar1.succeeded()) {
                    HttpClientRequest request = ar1.result();

                    // 发送请求并处理响应
                    request.send("Hello World", ar -> {
                        if (ar.succeeded()) {
                            HttpClientResponse resp = ar.result();
                            resp.headers().forEach( e ->{
                                System.out.println(e.getKey() + ":" + e.getValue());
                            });
                            resp.handler(b -> {
                                System.out.println("body:"+ b);
                            });

                            Future<HttpClientRequest> req2 = client.request(HttpMethod.GET, 8090, "localhost", "/web/userInfo");

                            req2.onFailure(err -> context.fail(err.getMessage()));
                            req2.onComplete(ar2 -> {
                                if (ar2.succeeded()) {
                                    HttpClientRequest request2 = ar2.result();
                                    request2.headers().addAll(resp.headers());

                                    // 发送请求并处理响应
                                    request2.send("Hello World", ar3 -> {
                                        if (ar3.succeeded()) {
                                            HttpClientResponse resp2 = ar3.result();
                                            resp2.handler(b -> System.out.println("final:"+b));
                                            context.assertEquals(200, resp2.statusCode());
                                        } else {
                                            System.out.println("Something went wrong " + ar3.cause().getMessage());
                                        }
                                    });
                                }

                                async.complete();
                            });

                        } else {
                            System.out.println("Something went wrong " + ar.cause().getMessage());
                        }
                        async.complete();
                    });
                }

            });
        });

        suite.run(options).await(100000);

    }
}
