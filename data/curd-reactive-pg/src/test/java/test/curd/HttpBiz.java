package test.curd;

import com.google.common.collect.Lists;
import com.tollge.common.SqlAndParams;
import com.tollge.common.annotation.mark.Biz;
import com.tollge.common.annotation.mark.Path;
import com.tollge.common.annotation.valid.NotNull;
import com.tollge.common.verticle.BizVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Biz("biz://tt")
public class HttpBiz extends BizVerticle {

    @Path("/fetchOne")
    @NotNull(key="a", msg="a is test key, it can't be null")
    public void fetchOne(Message<JsonObject> msg) {
        this.one("testDB.fetchOne", msg,
                new JsonObject().put("a", msg.body().getString("a") + " response"));
    }

    @Path("/one")
    @NotNull(key="a", msg="a is test key, it can't be null")
    public void one(Message<JsonObject> msg) {
//        DaoVerticle dao = MyDao.getDao();
        this.one("testDB.one", msg,
                new JsonObject().put("id", msg.body().getLong("a")));
    }
    
    @Path("/transaction")
    public void transaction(Message<JsonObject> msg) {
        List<SqlAndParams> sqlAndParamsList = Lists.newArrayList();
        // 注册用户
        JsonObject user = new JsonObject();
        user.put("userId", 123456L);
        user.put("mobile", "13800138000");
        user.put("tag", Lists.newArrayList());
        user.put("createUserId", 0L);
        user.put("updateUserId", 0L);
        SqlAndParams userSqlAndParams = new SqlAndParams("testDB.save1", user);
        
        // 添加密码
        JsonObject accountPsw = new JsonObject();
        accountPsw.put("userId", 123456L);
        accountPsw.put("psw", "123456");
        accountPsw.put("createUserId", 0L);
        accountPsw.put("updateUserId", 0L);
        SqlAndParams accountPswSqlAndParams = new SqlAndParams("testDB.save2", accountPsw);
        
        // 添加角色
        JsonObject userRole = new JsonObject();
        userRole.put("userId", 123456L);
        userRole.put("createUserId", 0L);
        userRole.put("updateUserId", 0L);
        userRole.put("roleList", Lists.newArrayList());
        SqlAndParams userRoleSqlAndParams = new SqlAndParams("testDB.save3", userRole);
        
        
        // 初始化水印
        JsonObject watermark = new JsonObject();
        watermark.put("userId", 123456L);
        watermark.put("createUserId", 0L);
        watermark.put("updateUserId", 0L);
        watermark.put("config", Lists.newArrayList());
        
        SqlAndParams watermarkSqlAndParams = new SqlAndParams("testDB.save4", watermark);
        
        sqlAndParamsList.add(userSqlAndParams);
        sqlAndParamsList.add(accountPswSqlAndParams);
        sqlAndParamsList.add(userRoleSqlAndParams);
        sqlAndParamsList.add(watermarkSqlAndParams);
        
        transaction(sqlAndParamsList, resC -> {
            if (resC.succeeded()) {
                log.info("register执行完成"+resC.result());
            } else {
                log.error("注册失败", resC.cause());
            }
        });
    }
}
