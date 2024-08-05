package test.curd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.tollge.MainVerticle;
import com.tollge.common.util.Properties;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestOptions;
import io.vertx.ext.unit.TestSuite;
import io.vertx.ext.unit.report.ReportOptions;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author toyer
 * @date 2018-10-23
 */
public class Tester {
    private Vertx vertx;

    @Test
    public void run() {
        TestOptions options = new TestOptions().addReporter(new ReportOptions().setTo("console"));
        TestSuite suite = TestSuite.create("test - curd");
        suite.before(ts -> {
            vertx = Vertx.vertx();
            vertx.deployVerticle(new MainVerticle(), ts.asyncAssertSuccess());
        });
        suite.after(ts -> {
            System.out.println("server close:"+ LocalDateTime.now());
            vertx.close(ts.asyncAssertSuccess());
        });

        suite.test("testDB", context -> {
            String jsonFormat = Properties.getString("application", "json.format", "yyyy-MM-dd HH:mm:ss");
            ObjectMapper objectMapper = DatabindCodec.mapper();
            JavaTimeModule module = new JavaTimeModule();
            LocalDateTimeDeserializer localDateTimeDeserializer =  new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(jsonFormat));
            module.addDeserializer(LocalDateTime.class, localDateTimeDeserializer);
            LocalDateTimeSerializer localDateTimeSerializer = new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(jsonFormat));
            module.addSerializer(localDateTimeSerializer);
            objectMapper.registerModule(module);

            Async async = context.async();
            vertx.eventBus().request("biz://tt/one", new JsonObject().put("a", 1), reply -> {
                if(reply.succeeded()) {
                    System.out.println("final: "+reply.result().body());
                } else {
                    reply.cause().printStackTrace();
                    System.err.println(reply.cause().getMessage());
                }
                async.complete();
            });
        });

        suite.run(options).await(100000);
    }
}
