package test.http;

import com.tollge.MainVerticle;
import io.vertx.core.Vertx;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author toyer
 * @date 2018-10-23
 */
@ExtendWith(VertxExtension.class)
public class Tester {

    // Deploy the verticle and execute the test methods when the verticle
    // is successfully deployed
    @BeforeEach
    void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new MainVerticle(), testContext.succeedingThenComplete());
    }

    @RepeatedTest(1)
    @Timeout(600000)
    void http_server_check_response(Vertx vertx, VertxTestContext testContext) {
    }
}
