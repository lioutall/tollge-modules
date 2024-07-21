package com.tollge.modules.curd.reactivepg;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import com.tollge.common.Page;
import com.tollge.common.SqlAndParams;
import com.tollge.common.TollgeException;
import com.tollge.common.util.Const;
import com.tollge.common.util.Properties;
import com.tollge.common.verticle.AbstractDao;
import com.tollge.sql.SqlEngineException;
import com.tollge.sql.SqlSession;
import com.tollge.sql.SqlTemplate;
import io.netty.util.internal.StringUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.impl.ArrayTuple;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tollge.common.BaseConstants.RETURN_CLASS_TYPE;

/**
 * reactive-pg-client数据库调用Verticle
 *
 * @author toyer
 */
@Slf4j
public class DaoVerticle extends AbstractDao {
    private Pool jdbcClient;

    @Override
    protected void init() {

        PgConnectOptions connectOptions = new PgConnectOptions(getDbConfig());

        // Create the pooled client
        jdbcClient = PgBuilder.pool().with(new PoolOptions()).connectingTo(connectOptions).using(vertx).build();
    }

    @Override
    protected void count(Message<JsonObject> msg) {
        final SqlAndParams sqlAndParams = fetchSqlAndParams(msg);

        jdbcClient.getConnection(connection -> {
            if (connection.succeeded()) {
                SqlConnection conn = connection.result();
                SqlSession sqlSession = getSqlSession(msg, sqlAndParams);
                if (sqlSession == null) {
                    return;
                }
                conn.preparedQuery(sqlSession.getSql()).execute(jsonArray2Tuple(sqlSession.getParams()), res -> {
                    if (res.succeeded()) {
                        try {
                            RowIterator<Row> ite = res.result().iterator();
                            Row r = ite.next();
                            Long result = r.getLong(0);
                            msg.reply(result);
                        } catch (Exception e) {
                            log.error("count.buildResult failed", e);
                            msg.fail(501, e.getMessage());
                        } finally {
                            conn.close();
                        }
                    } else {
                        log.error("count.query failed", res.cause());
                        msg.fail(501, res.cause().toString());
                        conn.close();
                    }
                });
            } else {
                log.error("count.getConnection failed", connection.cause());
                msg.fail(501, connection.cause().toString());
            }
        });
    }



    private SqlSession getSqlSession(Message<?> msg, SqlAndParams sqlAndParams) {
        try {
            SqlSession r = getRealSql(sqlAndParams.getSqlKey(), sqlAndParams.getParams());
            StringBuilder sb = new StringBuilder();
            int j = 1;
            for(char b : r.getSql().toCharArray()) {
                if(b == '?') {
                    sb.append('$');
                    sb.append(j++);
                } else {
                    sb.append(b);
                }
            }
            r.setSql(sb.toString());
            return r;
        } catch (Exception e) {
            log.error(GET_REAL_SQL_FAILED, sqlAndParams.getSqlKey(), e);
            msg.fail(501, e.toString());
            return null;
        }
    }

    @Override
    protected void page(Message<JsonObject> msg) {
        final SqlAndParams sqlAndParams = fetchSqlAndParams(msg);

        if (sqlAndParams.getLimit() == 0) {
            msg.fail(500, "limit should not be 0.");
            return;
        }

        jdbcClient.getConnection(connection -> {
            if (connection.succeeded()) {
                SqlConnection conn = connection.result();
                SqlSession sqlSession = getSqlSession(msg, sqlAndParams);
                if (sqlSession == null) {
                    return;
                }
                conn.preparedQuery("select count(1) from (".concat(sqlSession.getSql()).concat(") tab")).execute( jsonArray2Tuple(sqlSession.getParams()), getCount -> {
                    if (getCount.succeeded()) {
                        // 返回结果
                        JsonObject result = new JsonObject();
                        // 获得数据总行数
                        RowIterator<Row> ite = getCount.result().iterator();
                        Row r = ite.next();
                        Long count = r.getLong(0);

                        if(count == 0) {
                            Page rPage = new Page(sqlAndParams.getOffset()/sqlAndParams.getLimit() + 1 , sqlAndParams.getLimit());
                            rPage.setTotal(count);
                            msg.reply(rPage);
                            conn.close();
                            return;
                        }

                        // 执行获得数据结果
                        conn.preparedQuery(sqlSession.getSql().concat(" limit " + sqlAndParams.getLimit() + " offset " + sqlAndParams.getOffset())).execute( jsonArray2Tuple(sqlSession.getParams()), getData -> {
                            if (getData.succeeded()) {
                                JsonArray rows = under2Camel(getData);
                                Page rPage = new Page(sqlAndParams.getOffset()/sqlAndParams.getLimit() + 1 , sqlAndParams.getLimit());
                                rPage.setTotal(count);

                                try {
                                    String headerCls = msg.headers().get(RETURN_CLASS_TYPE);
                                    Class cCls = null;
                                    if(!StringUtil.isNullOrEmpty(headerCls)){
                                        cCls = Class.forName(headerCls);
                                        for (int i = 0; i < rows.size(); i++) {
                                            rPage.add(rows.getJsonObject(i).mapTo(cCls));
                                        }
                                    } else {
                                        for (int i = 0; i < rows.size(); i++) {
                                            rPage.add(rows.getJsonObject(i));
                                        }
                                    }
                                    msg.reply(rPage);
                                } catch (ClassNotFoundException e) {
                                    msg.fail(501, RETURN_CLASS_TYPE + " is not defined, value=" + msg.headers().get(RETURN_CLASS_TYPE));
                                }
                                conn.close();
                            } else {
                                log.error("page.getData failed", getData.cause());
                                msg.fail(501, getData.cause().toString());
                                conn.close();
                            }
                        });
                    } else {
                        log.error("page.getCount failed", getCount.cause());
                        msg.fail(501, getCount.cause().toString());
                        conn.close();
                    }
                });
            } else {
                log.error("page.getConnection failed", connection.cause());
                msg.fail(501, connection.cause().toString());
            }
        });
    }

    @Override
    protected void list(Message<JsonObject> msg) {
        final SqlAndParams sqlAndParams = fetchSqlAndParams(msg);
        jdbcClient.getConnection(connection -> {
            if (connection.succeeded()) {
                SqlConnection conn = connection.result();
                SqlSession sqlSession = getSqlSession(msg, sqlAndParams);
                if (sqlSession == null) {
                    return;
                }
                conn.preparedQuery(sqlSession.getSql()).execute( jsonArray2Tuple(sqlSession.getParams()), res -> {
                    if (res.succeeded()) {
                        try {
                            String headerCls = msg.headers().get(RETURN_CLASS_TYPE);
                            if(!StringUtil.isNullOrEmpty(headerCls)){
                                msg.reply(under2Camel(res, Class.forName(headerCls)));
                            } else {
                                msg.reply(under2Camel(res));
                            }
                        } catch (ClassNotFoundException e) {
                            msg.fail(501, RETURN_CLASS_TYPE + " is not defined, value=" + msg.headers().get(RETURN_CLASS_TYPE));
                        }
                        conn.close();
                    } else {
                        log.error("list.query failed", res.cause());
                        msg.fail(501, res.cause().toString());
                        conn.close();
                    }
                });
            } else {
                log.error("list.getConnection failed", connection.cause());
                msg.fail(501, connection.cause().toString());
            }
        });
    }

    @Override
    protected void one(Message<JsonObject> msg) {
        final SqlAndParams sqlAndParams = fetchSqlAndParams(msg);
        jdbcClient.getConnection(connection -> {
            if (connection.succeeded()) {
                SqlConnection conn = connection.result();
                SqlSession sqlSession = getSqlSession(msg, sqlAndParams);
                if (sqlSession == null) {
                    return;
                }
                conn.preparedQuery(sqlSession.getSql()).execute( jsonArray2Tuple(sqlSession.getParams()), res -> {
                    if (res.succeeded()) {
                        JsonObject result = null;
                        JsonArray rows = under2Camel(res);
                        if (!rows.isEmpty()) {
                            result = rows.getJsonObject(0);
                        }
                        try {
                            String headerCls = msg.headers().get(RETURN_CLASS_TYPE);
                            if(!StringUtil.isNullOrEmpty(headerCls)){
                                msg.reply(result.mapTo(Class.forName(headerCls)));
                            } else {
                                msg.reply(result);
                            }
                        } catch (ClassNotFoundException e) {
                            msg.fail(501, RETURN_CLASS_TYPE + " is not defined, value=" + msg.headers().get(RETURN_CLASS_TYPE));
                        }
                        conn.close();
                    } else {
                        log.error("one.query failed", res.cause());
                        conn.close();
                        msg.fail(501, res.cause().toString());
                    }
                });
            } else {
                log.error("one.getConnection failed", connection.cause());
                msg.fail(501, connection.cause().toString());
            }
        });
    }

    @Override
    protected void operate(Message<JsonObject> msg) {
        final SqlAndParams sqlAndParams = fetchSqlAndParams(msg);
        jdbcClient.getConnection(connection -> {
            if (connection.succeeded()) {
                SqlConnection conn = connection.result();
                SqlSession sqlSession = getSqlSession(msg, sqlAndParams);
                if (sqlSession == null) {
                    return;
                }
                conn.preparedQuery(sqlSession.getSql()).execute( jsonArray2Tuple(sqlSession.getParams()), res -> {
                    if (res.succeeded()) {
                        RowSet<Row> rows = res.result();

                        if(rows.columnsNames() != null && !rows.columnsNames().isEmpty()) {
                            RowIterator<Row> ite = rows.iterator();

                            List result = Lists.newArrayList();
                            Class cCls = null;
                            try {
                                String headerCls = msg.headers().get(RETURN_CLASS_TYPE);
                                if(!StringUtil.isNullOrEmpty(headerCls)){
                                    cCls = Class.forName(headerCls);
                                }
                            } catch (ClassNotFoundException e) {
                                msg.fail(501, RETURN_CLASS_TYPE + " is not defined, value=" + msg.headers().get(RETURN_CLASS_TYPE));
                                return;
                            }

                            while (ite.hasNext()) {
                                Row r = ite.next();
                                JsonObject jo = new JsonObject();
                                for (int i = 0; i < rows.columnsNames().size(); i++) {
                                    jo.put(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, rows.columnsNames().get(i)), r.getValue(i));
                                }

                                if(cCls != null){
                                    result.add(jo.mapTo(cCls));
                                } else {
                                    result.add(jo);
                                }
                            }
                            msg.reply(result);
                        } else {
                            msg.reply(rows.rowCount());
                        }

                        conn.close();
                    } else {
                        log.error("operate.query failed", res.cause());
                        conn.close();
                        msg.fail(501, res.cause().toString());
                    }
                });
            } else {
                log.error("operate.getConnection failed", connection.cause());
                msg.fail(501, connection.cause().toString());
            }
        });
    }

    @Override
    protected void batch(Message<JsonArray> msg) {
        List<SqlAndParams> sqlAndParamsList = fetchSqlAndParamsList(msg);
        if (sqlAndParamsList == null || sqlAndParamsList.isEmpty()) {
            msg.fail(501, "batch 为空, 请检查");
            return;
        }

        jdbcClient.getConnection(connection -> {
            if (connection.succeeded()) {
                SqlConnection conn = connection.result();

                List<SqlSession> list = null;
                try {
                    list = sqlAndParamsList.stream().map(sp ->
                            getRealSql(sp.getSqlKey(), sp.getParams())).collect(Collectors.toList());
                } catch (Exception e) {
                    log.error(GET_REAL_SQL_FAILED, sqlAndParamsList.get(0).getSqlKey(), e);
                    msg.fail(501, e.toString());
                    return;
                }
                String sqlId = list.get(0).getSql();
                conn.preparedQuery(sqlId).executeBatch( list.stream().map(se -> jsonArray2Tuple(se.getParams())).collect(Collectors.toList()), res -> {
                    if (res.succeeded()) {
                        int result = res.result().size();
                        msg.reply(result);
                        conn.close();
                    } else {
                        log.error("batch.batch[{}] failed", sqlId, connection.cause());
                        conn.close();
                        msg.fail(501, res.cause().toString());
                    }
                });
            } else {
                log.error("batch.getConnection failed", connection.cause());
                msg.fail(501, connection.cause().toString());
            }
        });
    }

    @Override
    protected void transaction(Message<JsonArray> msg) {
        // 获取操作列表
        final List<SqlAndParams> sqlAndParamsList = fetchSqlAndParamsList(msg);

        // 是否忽略执行结果
        String ignore = msg.headers().get(Const.IGNORE);

        // 获取连接
        jdbcClient.getConnection(connection -> {
            if (connection.succeeded()) {

                // Transaction must use a connection
                SqlConnection conn = connection.result();

                // Begin the transaction
                conn.begin().compose(tx -> {
                    Future<RowSet<Row>> updates = Future.future(c->log.info("create PgRowSet Future"));

                    for (SqlAndParams sqlParam : sqlAndParamsList) {
                        final SqlSession sqlSession = getRealSql(sqlParam.getSqlKey(), sqlParam.getParams());
                        updates = updates.compose(r -> Future.<RowSet<Row>>future(
                                ar -> conn.preparedQuery(sqlSession.getSql()).execute(jsonArray2Tuple(sqlSession.getParams()), ar)
                        ).onComplete(a -> {
                            if(a.succeeded()) {
                                if ("0".equals(ignore)){
                                    RowIterator<Row> ite = a.result().iterator();
                                    Row row = ite.next();
                                    Object result = row.getValue(0);
                                    if(result instanceof Integer && ((Integer) result) == 0) {
                                        throw new TollgeException("no update: " + sqlParam.getSqlKey());
                                    }
                                }
                            } else {
                                throw new TollgeException("transaction update error: " + sqlParam.getSqlKey());
                            }
                        }));
                    }

                    return updates.onComplete(res -> {
                        if(res.succeeded()) {
                            // Commit the transaction
                            tx.commit(ar -> {
                                if (ar.succeeded()) {
                                    msg.reply(sqlAndParamsList.size());
                                } else {
                                    tx.rollback();
                                    msg.fail(501, ar.cause().getMessage());
                                }

                                // Return the connection to the pool
                                conn.close();
                            });
                        } else {
                            tx.rollback();
                            conn.close();
                        }
                    }
                    );
                })
                // Return the connection to the pool
                .eventually(() -> conn.close())
                .onSuccess(v -> System.out.println("Transaction succeeded"))
                .onFailure(err -> System.out.println("Transaction failed: " + err.getMessage()));
            } else {
                log.error("transaction.getConnection failed", connection.cause());
                msg.fail(501, connection.cause().toString());
            }
        });

    }

    @Override
    protected JsonObject getDbConfig() {
        Map<String, Object> configs = Properties.getGroup("jdbc");
        return new JsonObject(configs);
    }

    protected JsonArray under2Camel(AsyncResult<RowSet<Row>> getData) {
        JsonArray array = new JsonArray();
        RowSet<Row> rs = getData.result();
        List<String> columns = rs.columnsNames();

        for(Row r : rs) {
            JsonObject j = new JsonObject();
            for (String cn : columns) {
                j.put(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, cn), r.getValue(cn));
            }
            array.add(j);
        }
        return array;
    }

    protected <T> List<T> under2Camel(AsyncResult<RowSet<Row>> getData, Class<T> cls) {
        List<T> list = Lists.newArrayList();
        RowSet<Row> rs = getData.result();
        List<String> columns = rs.columnsNames();

        for(Row r : rs) {
            JsonObject j = new JsonObject();
            for (String cn : columns) {
                j.put(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, cn), r.getValue(cn));
            }
            list.add(j.mapTo(cls));
        }
        return list;
    }

    protected List<SqlAndParams> fetchSqlAndParamsList(Message<JsonArray> msg) {
        return msg.body().stream().map(o -> {
            try {
                if (o != null) {
                    return ((JsonObject) o).mapTo(SqlAndParams.class);
                }
            } catch (Exception e) {
                throw new SqlEngineException("object invalid, json:" + msg.body().toString());
            }
            throw new SqlEngineException("object is null, json:" + msg.body().toString());
        }).collect(Collectors.toList());
    }

    protected SqlAndParams fetchSqlAndParams(Message<JsonObject> msg) {
        SqlAndParams sqlAndParams = msg.body() != null ? msg.body().mapTo(SqlAndParams.class) : null;
        if (sqlAndParams == null) {
            throw new SqlEngineException("sqlAndParams is null");
        }
        return sqlAndParams;
    }

    protected SqlSession getRealSql(String sqlKey, Map<String, Object> params) {
        try {
            return SqlTemplate.generateSQL(sqlKey, params);
        } catch (RuntimeException e) {
            log.error("获取sql[{}]异常", sqlKey, e);
            throw e;
        }
    }

    private Tuple jsonArray2Tuple(List<Object> list) {
        Tuple t = new ArrayTuple(list.size());
        for (Object elt: list) {
            t.addValue(elt);
        }
        return t;
    }

}
