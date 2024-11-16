package com.tollge.modules.data.redis;

import com.google.common.collect.Lists;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Command;
import io.vertx.redis.client.RedisConnection;
import io.vertx.redis.client.Request;
import io.vertx.redis.client.Response;
import io.vertx.redis.client.impl.types.SimpleStringType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class MyRedis {
    private MyRedis() {
    }

    public static RedisConnection getConn() {
        if (Singleton.INSTANCE.getInstance().conn == null) {
            int i = 0;
            // 防止调用比连接来得早
            while (i < 10) {
                if (Singleton.INSTANCE.getInstance().conn != null) {
                    return Singleton.INSTANCE.getInstance().conn;
                }
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                    log.error("sleep error", e);
                }
                i++;
            }
        }
        return Singleton.INSTANCE.getInstance().conn;
    }

    static void init(RedisConnection conn) {
        Singleton.INSTANCE.init(conn);
    }

    private enum Singleton {
        // 单例
        INSTANCE;

        private MyRedis single;

        private Singleton() {
            single = new MyRedis();
        }

        public MyRedis getInstance() {
            return single;
        }

        void init(RedisConnection conn) {
            single.conn = conn;
        }
    }

    private RedisConnection conn;

    protected Handler<AsyncResult<Response>> replyHandler(Promise<Response> reply) {
        return result -> {
            if (result.succeeded()) {
                reply.complete(result.result());
            } else {
                reply.fail(result.cause());
            }
        };
    }

    public static Future<Response> get(String key) {
        return Future.future(reply ->
                getConn().send(Request.cmd(Command.GET).arg(key), reply)
        );
    }

    public static Future<Response> getOrDefault(String key, String def) {
        return Future.future(reply ->
                getConn().send(Request.cmd(Command.GET).arg(key), r -> {
                    if (r.succeeded()) {
                        if (r.result() == null) {
                            reply.handle(Future.succeededFuture(SimpleStringType.create(def)));
                        }
                    } else {
                        reply.handle(r);
                    }
                })
        );
    }

    public static Future<Response> set(String key, String value) {
        return Future.future(reply -> getConn().send(Request.cmd(Command.SET).arg(key).arg(value), reply));
    }

    public static Future<Response> set(String key, String value, long expireMillSeconds) {
        return Future.future(reply -> getConn().send(Request.cmd(Command.SET).arg(key).arg(value).arg(SET_WITH_EXPIRE_TIME).arg(expireMillSeconds), reply));
    }

    public static Future<Response> incr(String key) {
        return Future.future(reply -> getConn().send(Request.cmd(Command.INCR).arg(key), reply));
    }

    public static Future<Response> ttl(String key) {
        return Future.future(reply -> getConn().send(Request.cmd(Command.TTL).arg(key), reply));
    }

    public static Future<Response> expire(String key, long seconds) {
        return Future.future(reply -> getConn().send(Request.cmd(Command.EXPIRE).arg(key).arg(seconds), reply));
    }

    public static Future<Response> del(String key) {
        return Future.future(reply -> getConn().send(Request.cmd(Command.DEL).arg(key), reply));
    }

    public static Future<Response> mget(List<String> keyList) {
        return Future.future(reply -> {
            Request cmd = Request.cmd(Command.MGET);
            for (String s : keyList) {
                cmd.arg(s);
            }
            getConn().send(cmd, reply);
        });
    }

    public static Future<Response> mset(Map<String, String> m) {
        return Future.future(reply -> {
            Request cmd = Request.cmd(Command.MSET);
            for (Map.Entry<String, String> entry : m.entrySet()) {
                cmd.arg(entry.getKey()).arg(entry.getValue());
            }
            getConn().send(cmd, reply);
        });
    }


    /*****************************
     * 分布式锁
     **************************/

    private static final String LOCK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    /**
     * EX|PX, expire time units: EX = seconds; PX = milliseconds
     */
    private static final String SET_WITH_EXPIRE_TIME = "PX";

    /**
     * 尝试获取分布式锁
     *
     * @param lockKey    锁
     * @param requestId  请求标识
     * @param expireTime 超期时间
     * @return 是否获取成功
     */
    public static Future<Response> tryGetDistributedLock(String lockKey, String requestId, int expireTime) {
        return Future.future(reply -> MyRedis.getConn().send(Request.cmd(Command.SET)
                .arg(lockKey).arg(requestId).arg(SET_IF_NOT_EXIST).arg(SET_WITH_EXPIRE_TIME).arg(expireTime), reply));
    }

    public static Future<Response> tryGetDistributedLock(String lockKey, String requestId) {
        return tryGetDistributedLock(lockKey, requestId, 6000);
    }

    public static Future<Response> tryGetDistributedLock(String lockKey) {
        return tryGetDistributedLock(lockKey, Thread.currentThread().getName(), 6000);
    }

    private static final Long RELEASE_SUCCESS = 1L;
    private static final String RELEASE_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    /**
     * 释放分布式锁
     *
     * @param lockKey   锁
     * @param requestId 请求标识
     * @return 是否释放成功
     */
    public static Future<Response> releaseDistributedLock(String lockKey, String requestId) {
        return Future.future(reply -> MyRedis.getConn().send(Request.cmd(Command.EVAL).arg(RELEASE_SCRIPT).arg(1).arg(lockKey).arg(requestId), reply));
    }

    public static Future<Response> releaseDistributedLock(String lockKey) {
        return releaseDistributedLock(lockKey, Thread.currentThread().getName());
    }
    
    /**
     * 缓存
     */
    public static <T> Future<T> cache(String key, String prefix, int expireSeconds, Class<T> clazz, Future<T> future) {
        // 转成redis key
        String redisKey = prefix + key;
        // 获取缓存
        return get(redisKey)
                .compose(response -> {
                     if (response != null) {
                         if(clazz == String.class){
                             return Future.succeededFuture((T) response.toString());
                         }
                         return Future.succeededFuture(new JsonObject(response.toString()).mapTo(clazz));
                     }
                     
                     return future.compose(result -> {
                         // 设置缓存
                         set(redisKey, result.toString(), expireSeconds * 1000L);
                         if(clazz == String.class){
                             return Future.succeededFuture((T) response.toString());
                         }
                         return Future.succeededFuture(new JsonObject(response.toString()).mapTo(clazz));
                     });
                })
                .onFailure(error -> log.error("cache error", error));
    }
    
    public static Future<String> cache(String key, String prefix, int expireSeconds, Future<String> future) {
        return cache(key, prefix, expireSeconds, String.class, future);
    }
    
    public static <T> Future<List<T>> cacheList(List<?> keyList, String prefix, int expireSeconds, Class<T> clazz, Function<List<String>, Future<Map<?, T>>> function) {
        if(keyList == null || keyList.isEmpty()) {
            return Future.succeededFuture(Lists.newArrayList());
        }
        // keyList 转成 redis key
        List<String> redisKeyList = keyList.stream().map(k -> prefix + k).collect(Collectors.toList());
        return MyRedis.mget(redisKeyList)
               .compose(res -> {
                   List<T> resultList = Lists.newArrayList();
                   List missKeyList = Lists.newArrayList();
                   for (int i = 0; i < keyList.size(); i++) {
                       Response response = res.get(i);
                       if (response == null) {
                           missKeyList.add(keyList.get(i));
                       }
                   }
                   
                   if (!missKeyList.isEmpty()) {
                       Future<Map<Object, T>> future = function.apply(missKeyList);
                       return future
                               .compose(missRes -> {
                                   MyRedis.mset(missRes.entrySet().stream().collect(Collectors.toMap(c -> prefix + c.getKey(), c -> Json.encode(c.getValue()))));
                                   missRes.forEach((key, value) -> MyRedis.expire(prefix + key, expireSeconds));
                                   for (int i = 0; i < keyList.size(); i++) {
                                       Response response = res.get(i);
                                       if (response != null) {
                                           resultList.add(new JsonObject(response.toString()).mapTo(clazz));
                                       } else {
                                           resultList.add(missRes.get(keyList.get(i)));
                                       }
                                   }
                                   return Future.succeededFuture(resultList);
                               });
                   } else {
                       for (int i = 0; i < keyList.size(); i++) {
                           Response response = res.get(i);
                           resultList.add(new JsonObject(response.toString()).mapTo(clazz));
                       }
                       
                       return Future.succeededFuture(resultList);
                   }
               })
               .onFailure(err -> log.error("cacheList error", err));
    }

}