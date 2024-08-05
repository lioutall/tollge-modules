package com.tollge.modules.oss.qiniu;

import com.qiniu.util.Auth;
import com.tollge.common.annotation.mark.Biz;
import com.tollge.common.util.Properties;
import com.tollge.common.verticle.BizVerticle;
import lombok.extern.slf4j.Slf4j;

/**
 * OSS服务器
 */
@Slf4j
@Biz(value = "third://oss/qiniu", worker = true)
public class QiniuVerticle extends BizVerticle {

    private static final String GROUP = "oss.qiniu";
    private long cacheSeconds = 300;

    private Auth auth = Auth.create(Properties.getString(GROUP, "accessKey"),
            Properties.getString(GROUP, "secretKey"));

    private static final String BUCKET = Properties.getString(GROUP, "bucket");

    // 5分钟自动刷新, 30分钟未访问则停止
   /* private RefreshPolicy policy = RefreshPolicy.newPolicy(cacheSeconds, TimeUnit.SECONDS)
            .stopRefreshAfterLastAccess(30, TimeUnit.MINUTES);

    // 内存缓存
    private Cache<String, String> cache = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
            .expireAfterWrite(cacheSeconds+30, TimeUnit.SECONDS)
            .loader(key -> {
                log.debug("begin fetch token!");
                return auth.uploadToken(BUCKET, null, cacheSeconds+30, new StringMap());
            })
            .refreshPolicy(policy)
            .buildCache();

    *//**
     * 获取token
     *
     * @param msg []
     *//*
    @Path("/uploadToken")
    public void uploadToken(Message<JsonObject> msg) {
        msg.reply(cache.get(""));
    }*/

}
