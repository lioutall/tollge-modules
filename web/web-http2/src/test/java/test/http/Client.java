package test.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.*;

/*
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class Client extends AbstractVerticle {

  // Convenience method so you can run it in your IDE
  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new Client());
  }

  @Override
  public void start() throws Exception {

    // Note! in real-life you wouldn't often set trust all to true as it could leave you open to man in the middle attacks.

    HttpClientOptions options = new HttpClientOptions().
        setSsl(true).
        setUseAlpn(true).
        setProtocolVersion(HttpVersion.HTTP_2).
        setTrustAll(true);

    vertx.createHttpClient(options).request(HttpMethod.GET, 8443, "localhost", "/web/test/aaa", ar1 -> {

      if (ar1.succeeded()) {
        HttpClientRequest request = ar1.result();

        // 发送请求并处理响应
        request.send("Hello World", ar -> {
          if (ar.succeeded()) {
            HttpClientResponse resp = ar.result();
            System.out.println("Got response " + resp.statusCode() + " with protocol " + resp.version());
            resp.bodyHandler(body -> System.out.println("Got data " + body.toString("ISO-8859-1")));
          } else {
            System.out.println("Something went wrong " + ar.cause().getMessage());
          }
        });
      }

    });
  }
}
