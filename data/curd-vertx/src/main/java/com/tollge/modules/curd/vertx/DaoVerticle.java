package com.tollge.modules.curd.vertx;

import com.google.common.base.CaseFormat;
import com.tollge.common.SqlAndParams;
import com.tollge.common.TollgeException;
import com.tollge.common.util.Const;
import com.tollge.common.util.Properties;
import com.tollge.common.verticle.AbstractDao;
import com.tollge.sql.SqlEngineException;
import com.tollge.sql.SqlSession;
import com.tollge.sql.SqlTemplate;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 标准数据库调用Verticle
 *
 * @author toyer
 */
@Slf4j
public class DaoVerticle extends AbstractDao {
    private JDBCClient jdbcClient;

    @Override
    protected void init() {
        jdbcClient = JDBCClient.createShared(vertx, getDbConfig());
    }

    @Override
    protected void count(Message<JsonObject> msg) {
        final SqlAndParams sqlAndParams = fetchSqlAndParams(msg);

        jdbcClient.getConnection(connection -> {
            if (connection.succeeded()) {
                SQLConnection conn = connection.result();
                SqlSession sqlSession = getSqlSession(msg, sqlAndParams);
                if (sqlSession == null) {
                    return;
                }
                conn.queryWithParams(sqlSession.getSql(), new JsonArray(sqlSession.getParams()), res -> {
                    if (res.succeeded()) {
                        try {
                            Long result = res.result().getResults().get(0).getLong(0);
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
            return getRealSql(sqlAndParams.getSqlKey(), sqlAndParams.getParams());
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
                SQLConnection conn = connection.result();
                SqlSession sqlSessionCount = getSqlSession(msg, sqlAndParams);
                if (sqlSessionCount == null) {
                    return;
                }
                conn.queryWithParams("select count(1) from (".concat(sqlSessionCount.getSql()).concat(") tab"), new JsonArray(sqlSessionCount.getParams()), getCount -> {
                    if (getCount.succeeded()) {
                        // 返回结果
                        JsonObject result = new JsonObject();
                        // 获得数据总行数
                        Long count = getCount.result().getResults().get(0).getLong(0);
                        // 执行获得数据结果
                        SqlSession sqlSession = getSqlSession(msg, sqlAndParams);
                        if (sqlSession == null) {
                            return;
                        }
                        conn.queryWithParams(sqlSession.getSql().concat(" limit " + sqlAndParams.getLimit() + " offset " + sqlAndParams.getOffset()), new JsonArray(sqlSession.getParams()), getData -> {
                            if (getData.succeeded()) {
                                ResultSet rs = under2Camel(getData);
                                List<JsonObject> rows = rs.getRows();
                                result.put(Const.TOLLGE_PAGE_COUNT, count);
                                result.put(Const.TOLLGE_PAGE_DATA, new JsonArray(rows));
                                msg.reply(result);
                                conn.close();
                            } else {
                                log.error("count.getData failed", getData.cause());
                                msg.fail(501, getData.cause().toString());
                                conn.close();
                            }
                        });
                    } else {
                        log.error("count.getCount failed", getCount.cause());
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
                SQLConnection conn = connection.result();
                SqlSession sqlSession = getSqlSession(msg, sqlAndParams);
                if (sqlSession == null) {
                    return;
                }
                conn.queryWithParams(sqlSession.getSql(), new JsonArray(sqlSession.getParams()), res -> {
                    if (res.succeeded()) {
                        ResultSet rs = under2Camel(res);
                        List<JsonObject> rows = rs.getRows();
                        msg.reply(new JsonArray(rows));
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
                SQLConnection conn = connection.result();
                SqlSession sqlSession = getSqlSession(msg, sqlAndParams);
                if (sqlSession == null) {
                    return;
                }
                conn.queryWithParams(sqlSession.getSql(), new JsonArray(sqlSession.getParams()), res -> {
                    if (res.succeeded()) {
                        JsonObject result = null;
                        ResultSet rs = under2Camel(res);
                        List<JsonObject> rows = rs.getRows();
                        if (!rows.isEmpty()) {
                            result = rows.get(0);
                        }
                        msg.reply(result);
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
                SQLConnection conn = connection.result();
                SqlSession sqlSession = getSqlSession(msg, sqlAndParams);
                if (sqlSession == null) {
                    return;
                }
                conn.updateWithParams(sqlSession.getSql(), new JsonArray(sqlSession.getParams()), res -> {
                    if (res.succeeded()) {
                        if (res.result().getKeys() != null && !res.result().getKeys().isEmpty()) {
                            msg.reply(res.result().getKeys());
                        } else {
                            msg.reply(res.result().getUpdated());
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
                SQLConnection conn = connection.result();

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
                conn.batchWithParams(sqlId, list.stream().map(se -> new JsonArray(se.getParams())).collect(Collectors.toList()), res -> {
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

        jdbcClient.getConnection(connR -> {
            SQLConnection conn = connR.result();
            if(connR.succeeded()) {
                // 是否忽略执行结果
                String ignore = msg.headers().get(Const.IGNORE);

                // 设置成手动提交
                conn.setAutoCommit(false, r -> {
                    if(r.succeeded()) {
                        Future<UpdateResult> deals = Future.future(Future::complete);

                        for (SqlAndParams sqlParam : sqlAndParamsList) {
                            SqlSession sqlSession = getRealSql(sqlParam.getSqlKey(), sqlParam.getParams());

                            deals = deals.compose(deal -> Future.<UpdateResult>future(f->{
                                conn.updateWithParams(sqlSession.getSql(), new JsonArray(sqlSession.getParams()), f);
                            }).setHandler(a -> {
                                if (a.succeeded()) {
                                    if ("0".equals(ignore)){
                                        int result = a.result().getUpdated();
                                        if(result == 0) {
                                            throw new TollgeException("no update: " + sqlParam.getSqlKey());
                                        }
                                    }
                                } else {
                                    throw new TollgeException("transaction update error: " + sqlParam.getSqlKey());
                                }
                            }));
                        }

                        deals.setHandler(res -> {
                            if(res.succeeded()) {
                                conn.commit(commitR -> {
                                    if(commitR.succeeded()) {
                                        setAutoCommitAndClose(conn);
                                        msg.reply(sqlAndParamsList.size());
                                    } else {
                                        log.error("transaction.commit error", commitR.cause());
                                        rollback(msg, conn);
                                        msg.fail(501, commitR.cause().toString());
                                    }
                                });
                            } else {
                                log.error("transaction error", res.cause());
                                rollback(msg, conn);
                                msg.fail(501, res.cause().toString());
                            }
                        });

                    } else {
                        log.error("transaction error", r.cause());
                        setAutoCommitAndClose(conn);
                        msg.fail(501, r.cause().toString());
                    }
                });

            } else {
                log.error("transaction.connect error", connR.cause());
                if(conn != null) {
                    conn.close();
                }
                msg.fail(501, connR.cause().toString());
            }
        });
    }

    private void rollback(Message<JsonArray> msg, SQLConnection conn) {
        conn.rollback(rollRes -> {
            if (rollRes.succeeded()) {
                setAutoCommitAndClose(conn);
            } else {
                setAutoCommitAndClose(conn);
                log.error("rollback error:", rollRes.cause());
                msg.fail(501, rollRes.cause().toString());
            }
        });
    }

    private void setAutoCommitAndClose(SQLConnection conn) {
        conn.setAutoCommit(true, auto -> {
            if (!auto.succeeded()) {
                log.error("can not set autocommit", auto.cause());
            }
            conn.close();
        });
    }

    @Override
    protected JsonObject getDbConfig() {
        Map<String, Object> configs = Properties.getGroup("jdbc");
        return new JsonObject(configs);
    }

    protected ResultSet under2Camel(AsyncResult<ResultSet> getData) {
        ResultSet rs = getData.result();
        rs.setColumnNames(rs.getColumnNames().stream().map(a ->
                CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, a)).collect(Collectors.toList()));
        return rs;
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
}
