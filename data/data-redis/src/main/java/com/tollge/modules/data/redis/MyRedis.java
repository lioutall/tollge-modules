package com.tollge.modules.data.redis;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.redis.client.Command;
import io.vertx.redis.client.RedisConnection;
import io.vertx.redis.client.Request;
import io.vertx.redis.client.Response;
import io.vertx.redis.client.impl.types.SimpleStringType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

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

}