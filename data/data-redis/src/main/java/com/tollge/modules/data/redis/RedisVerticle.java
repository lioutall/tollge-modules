package com.tollge.modules.data.redis;

import com.tollge.common.util.Properties;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.net.NetClientOptions;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisClientType;
import io.vertx.redis.client.RedisConnection;
import io.vertx.redis.client.RedisOptions;
import lombok.extern.slf4j.Slf4j;

/**
 * redis调用Verticle
 *
 * @author toyer
 */
@Slf4j
public class RedisVerticle extends AbstractVerticle {

  @Override
  @SuppressWarnings("unchecked")
  public void start() {

    createRedisClient()
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

    NetClientOptions netclientOptions = new NetClientOptions().setReconnectInterval(1000).setReconnectAttempts(5).setConnectTimeout(1000);
    Redis client = Redis.createClient(vertx, new RedisOptions()
      .setType(RedisClientType.valueOf(Properties.getString("redis", "type", "STANDALONE")))
      .setNetClientOptions(netclientOptions)
      .addConnectionString(Properties.getString("redis", "connectionString", "")));
    MyRedis.init(client);
    return promise.future();
  }
}
