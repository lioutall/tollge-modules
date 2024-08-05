package com.tollge.modules.data.redis;

import com.tollge.common.util.Properties;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisClientType;
import io.vertx.redis.client.RedisConnection;
import io.vertx.redis.client.RedisOptions;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * redis调用Verticle
 *
 * @author toyer
 */
@Slf4j
public class RedisVerticle extends AbstractVerticle {

    private final AtomicBoolean CONNECTING = new AtomicBoolean();
    private RedisConnection client;

    @Override
    @SuppressWarnings("unchecked")
    public void start() {

            createRedisClient()
                .onSuccess(MyRedis::init)
                .onFailure(
                        e -> log.error("start redis verticle error", e.getCause())
                );
    }

    /**
     * Will create a redis client and setup a reconnect handler when there is
     * an exception in the connection.
     */
    private Future<RedisConnection> createRedisClient() {
        Promise<RedisConnection> promise = Promise.promise();

        // make sure to invalidate old connection if present
        if (client != null) {
            client.close();
        }

        if (CONNECTING.compareAndSet(false, true)) {
            Redis.createClient(vertx, new RedisOptions()
                            .setType(RedisClientType.valueOf(Properties.getString("redis", "type", "STANDALONE")))
                            .addConnectionString(Properties.getString("redis", "connectionString", "")))
                    .connect()
                    .onSuccess(conn -> {
                        client = conn;

                        // make sure the client is reconnected on error
                        // eg, the underlying TCP connection is closed but the client side doesn't know it yet
                        //     the client tries to use the staled connection to talk to server. An exceptions will be raised
                        conn.exceptionHandler(e -> {
                            attemptReconnect(0);
                        });

                        // make sure the client is reconnected on connection close
                        // eg, the underlying TCP connection is closed with normal 4-Way-Handshake
                        //     this handler will be notified instantly
                        conn.endHandler(placeHolder -> {
                            attemptReconnect(0);
                        });

                        // allow further processing
                        promise.complete(conn);
                        CONNECTING.set(false);
                    }).onFailure(t -> {
                        promise.fail(t);
                        CONNECTING.set(false);
                    });
        } else {
            promise.complete();
        }

        return promise.future();
    }

    /**
     * Attempt to reconnect up to MAX_RECONNECT_RETRIES
     */
    private void attemptReconnect(int retry) {
        if (retry > 10) {
            // we should stop now, as there's nothing we can do.
            CONNECTING.set(false);
        } else {
            // retry with backoff up to 10240 ms
            long backoff = (long) (Math.pow(2, Math.min(retry, 10)) * 10);

            vertx.setTimer(backoff, timer -> {
                createRedisClient().onFailure(t -> attemptReconnect(retry + 1));
            });
        }
    }
}
