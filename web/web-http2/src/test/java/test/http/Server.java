package test.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.PemKeyCertOptions;

/*
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class Server extends AbstractVerticle {

  // Convenience method so you can run it in your IDE
  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new Server());
  }

  @Override
  public void start() throws Exception {

    HttpServer server =
      vertx.createHttpServer(new HttpServerOptions().
          setUseAlpn(true).
          setSsl(true).
          setPemKeyCertOptions(new PemKeyCertOptions().setKeyPath("tls/server-key.pem").setCertPath("tls/server-cert.pem")
      ));

    server.requestHandler(req -> {
      req.response().putHeader("content-type", "text/html").end("<html><body>" +
          "<h1>Hello from vert.x!</h1>" +
          "<p>version = " + req.version() + "</p>" +
          "</body></html>");
              System.out.println("完成");
    }).listen(8443);
  }
}
