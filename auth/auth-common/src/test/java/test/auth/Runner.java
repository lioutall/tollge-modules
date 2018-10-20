package test.auth;

import com.tollge.MainVerticle;
import io.vertx.core.Vertx;

/**
 * 启动器
 *
 * @author toyer
 * @created 2018-01-31
 */
public class Runner {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
    }

    // after that, you can browser http://127.0.0.1:8090/web/test/mykey
}
