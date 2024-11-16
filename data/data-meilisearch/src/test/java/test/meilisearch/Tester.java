package test.meilisearch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tollge.MainVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.jackson.DatabindCodec;
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

        DatabindCodec codec = (DatabindCodec) io.vertx.core.json.Json.CODEC;
        ObjectMapper mapper = codec.mapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        TestOptions options = new TestOptions().addReporter(new ReportOptions().setTo("console"));
        TestSuite suite = TestSuite.create("test - redis");
        suite.before(ts -> {
            vertx = Vertx.vertx();
            vertx.deployVerticle(new MainVerticle(), ts.asyncAssertSuccess());
        });
        suite.after(ts -> {
            vertx.close(ts.asyncAssertFailure());
            System.out.println("server close:"+ LocalDateTime.now());

        });

        suite.test("testMeili", context -> {
            Async async = context.async();
            vertx.eventBus().request("biz://tt/testMeiliAdd", null, reply -> {
                if(reply.succeeded()) {
                    System.out.println("testMeiliAdd: "+reply.result().body());


                    vertx.eventBus().request("biz://tt/testGetOne", null, r -> {
                        if(r.succeeded()) {
                            System.out.println("testGetOne: "+r.result().body());
                        } else {
                            System.err.println(r.cause().getMessage());
                        }
                    });

                    vertx.eventBus().request("biz://tt/testMeiliSearch", null, r -> {
                        if(r.succeeded()) {
                            System.out.println("testMeiliSearch: "+r.result().body());
                        } else {
                            System.err.println(r.cause().getMessage());
                        }
                    });

                    vertx.eventBus().request("biz://tt/testmultiSearch", null, r -> {
                        if(r.succeeded()) {
                            System.out.println("testmultiSearch: "+ Json.encode(r.result().body()));
                        } else {
                            System.err.println(r.cause().getMessage());
                        }
                    });

                    vertx.eventBus().request("biz://tt/testmultiSearchFederate", null, r -> {
                        if(r.succeeded()) {
                            System.out.println("testmultiSearchFederate: "+ Json.encode(r.result().body()));

                            vertx.eventBus().request("biz://tt/testMeiliDelete", null, k -> {
                                if(k.succeeded()) {
                                    System.out.println("testMeiliDelete: "+k.result().body());
                                } else {
                                    System.err.println(k.cause().getMessage());
                                }
                            });
                        } else {
                            System.err.println(r.cause().getMessage());
                        }
                    });

                } else {
                    System.err.println(reply.cause().getMessage());
                }
            });
        });

        suite.run(options).await(100000);
    }
}
