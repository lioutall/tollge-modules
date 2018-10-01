package io.tollge.modules.oss.qiniu;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.RefreshPolicy;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import io.tollge.common.annotation.mark.Biz;
import io.tollge.common.annotation.mark.Path;
import io.tollge.common.util.Properties;
import io.tollge.common.verticle.BizVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * OSS服务器
 */
@Slf4j
@Biz(value = "third://oss/qiniu", worker = true)
public class QiniuVerticle extends BizVerticle {

    private static final String GROUP = "oss.qiniu";
    private long cacheSeconds = 300;

    // 5分钟自动刷新, 30分钟未访问则停止
    private RefreshPolicy policy = RefreshPolicy.newPolicy(cacheSeconds, TimeUnit.SECONDS)
            .stopRefreshAfterLastAccess(30, TimeUnit.MINUTES);

    // 内存缓存
    private Cache<String, String> cache = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
            .expireAfterWrite(cacheSeconds+30, TimeUnit.SECONDS)
            .loader(key -> {
                log.debug("begin fetch token!");
                Auth auth = Auth.create(Properties.getString(GROUP, "accessKey"),
                        Properties.getString(GROUP, "secretKey"));
                long expireSeconds = cacheSeconds+30;
                return auth.uploadToken(Properties.getString(GROUP, "bucket"), null, expireSeconds, new StringMap());
            })
            .refreshPolicy(policy)
            .buildCache();

    /**
     * 发送短信
     *
     * @param msg []
     */
    @Path("/uploadToken")
    public void uploadToken(Message<JsonObject> msg) {
        msg.reply(cache.get(""));
    }

}
