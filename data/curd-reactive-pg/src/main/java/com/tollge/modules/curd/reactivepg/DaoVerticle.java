package com.tollge.modules.curd.reactivepg;

import com.google.common.base.CaseFormat;
import com.tollge.common.SqlAndParams;
import com.tollge.common.util.Const;
import com.tollge.common.util.Properties;
import com.tollge.common.verticle.AbstractDao;
import com.tollge.sql.SqlEngineException;
import com.tollge.sql.SqlSession;
import com.tollge.sql.SqlTemplate;
import io.reactiverse.pgclient.*;
import io.reactiverse.pgclient.data.Json;
import io.reactiverse.pgclient.impl.ArrayTuple;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * reactive-pg-client数据库调用Verticle
 *
 * @author toyer
 */
@Slf4j
public class DaoVerticle extends AbstractDao {
    private PgPool jdbcClient;

    @Override
    protected void init() {
        jdbcClient = PgClient.pool(vertx, new PgPoolOptions(getDbConfig()));
    }

    @Override
    protected void count(Message<JsonObject> msg) {
        final SqlAndParams sqlAndParams = fetchSqlAndParams(msg);

        jdbcClient.getConnection(connection -> {
            if (connection.succeeded()) {
                PgConnection conn = connection.result();
                SqlSession sqlSession = getSqlSession(msg, sqlAndParams);
                if (sqlSession == null) {
                    return;
                }
                conn.preparedQuery(sqlSession.getSql(), jsonArray2Tuple(sqlSession.getParams()), res -> {
                    if (res.succeeded()) {
                        try {
                            PgIterator ite = res.result().iterator();
                            Row r = ite.next();
                            Long result = r.getLong(0);
                            msg.reply(result);
                        } catch (Exception e) {
                            msg.fail(501, e.getMessage());
                        } finally {
                            conn.close();
                        }
                    } else {
                        msg.fail(501, res.cause().toString());
                        conn.close();
                    }
                });
            } else {
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
                    sb.append(Integer.toString(j++));
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
                PgConnection conn = connection.result();
                SqlSession sqlSessionCount = getSqlSession(msg, sqlAndParams);
                if (sqlSessionCount == null) {
                    return;
                }
                conn.preparedQuery("select count(1) from (".concat(sqlSessionCount.getSql()).concat(") tab"), jsonArray2Tuple(sqlSessionCount.getParams()), getCount -> {
                    if (getCount.succeeded()) {
                        // 返回结果
                        JsonObject result = new JsonObject();
                        // 获得数据总行数
                        PgIterator ite = getCount.result().iterator();
                        Row r = ite.next();
                        Long count = r.getLong(0);
                        // 执行获得数据结果
                        SqlSession sqlSession = getSqlSession(msg, sqlAndParams);
                        if (sqlSession == null) {
                            return;
                        }
                        conn.preparedQuery(sqlSession.getSql().concat(" limit " + sqlAndParams.getLimit() + " offset " + sqlAndParams.getOffset()), jsonArray2Tuple(sqlSession.getParams()), getData -> {
                            if (getData.succeeded()) {
                                JsonArray rows = under2Camel(getData);
                                result.put(Const.TOLLGE_PAGE_COUNT, count);
                                result.put(Const.TOLLGE_PAGE_DATA, rows);
                                msg.reply(result);
                                conn.close();
                            } else {
                                msg.fail(501, getData.cause().toString());
                                conn.close();
                            }
                        });
                    } else {
                        msg.fail(501, getCount.cause().toString());
                        conn.close();
                    }
                });
            } else {
                msg.fail(501, connection.cause().toString());
            }
        });
    }

    @Override
    protected void list(Message<JsonObject> msg) {
        final SqlAndParams sqlAndParams = fetchSqlAndParams(msg);
        jdbcClient.getConnection(connection -> {
            if (connection.succeeded()) {
                PgConnection conn = connection.result();
                SqlSession sqlSession = getSqlSession(msg, sqlAndParams);
                if (sqlSession == null) {
                    return;
                }
                conn.preparedQuery(sqlSession.getSql(), jsonArray2Tuple(sqlSession.getParams()), res -> {
                    if (res.succeeded()) {
                        msg.reply(under2Camel(res));
                        conn.close();
                    } else {
                        msg.fail(501, res.cause().toString());
                        conn.close();
                    }
                });
            } else {
                msg.fail(501, connection.cause().toString());
            }
        });
    }

    @Override
    protected void one(Message<JsonObject> msg) {
        final SqlAndParams sqlAndParams = fetchSqlAndParams(msg);
        jdbcClient.getConnection(connection -> {
            if (connection.succeeded()) {
                PgConnection conn = connection.result();
                SqlSession sqlSession = getSqlSession(msg, sqlAndParams);
                if (sqlSession == null) {
                    return;
                }
                conn.preparedQuery(sqlSession.getSql(), jsonArray2Tuple(sqlSession.getParams()), res -> {
                    if (res.succeeded()) {
                        JsonObject result = null;
                        JsonArray rows = under2Camel(res);
                        if (!rows.isEmpty()) {
                            result = rows.getJsonObject(0);
                        }
                        msg.reply(result);
                        conn.close();
                    } else {
                        conn.close();
                        msg.fail(501, res.cause().toString());
                    }
                });
            } else {
                msg.fail(501, connection.cause().toString());
            }
        });
    }

    @Override
    protected void operate(Message<JsonObject> msg) {
        final SqlAndParams sqlAndParams = fetchSqlAndParams(msg);
        jdbcClient.getConnection(connection -> {
            if (connection.succeeded()) {
                PgConnection conn = connection.result();
                SqlSession sqlSession = getSqlSession(msg, sqlAndParams);
                if (sqlSession == null) {
                    return;
                }
                conn.preparedQuery(sqlSession.getSql(), jsonArray2Tuple(sqlSession.getParams()), res -> {
                    if (res.succeeded()) {
                        PgIterator ite = res.result().iterator();
                        Row r = ite.next();
                        Object result = r.getValue(0);
                        if (result instanceof Json) {
                            msg.reply(((Json) result).value());
                        } else {
                            msg.reply(result);
                        }
                        conn.close();
                    } else {
                        conn.close();
                        msg.fail(501, res.cause().toString());
                    }
                });
            } else {
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
                PgConnection conn = connection.result();

                List<SqlSession> list = null;
                try {
                    list = sqlAndParamsList.stream().map(sp ->
                            getRealSql(sp.getSqlKey(), sp.getParams())).collect(Collectors.toList());
                } catch (Exception e) {
                    log.error(GET_REAL_SQL_FAILED, sqlAndParamsList.get(0).getSqlKey(), e);
                    msg.fail(501, e.toString());
                    return;
                }
                conn.preparedBatch(list.get(0).getSql(), list.stream().map(se -> jsonArray2Tuple(se.getParams())).collect(Collectors.toList()), res -> {
                    if (res.succeeded()) {
                        int result = res.result().size();
                        msg.reply(result);
                        conn.close();
                    } else {
                        conn.close();
                        msg.fail(501, res.cause().toString());
                    }
                });
            } else {
                msg.fail(501, connection.cause().toString());
            }
        });
    }

    @Override
    protected void transaction(Message<JsonArray> msg) {
        // 获取操作列表
        final List<SqlAndParams> sqlAndParamsList = fetchSqlAndParamsList(msg);

        // 获取连接
        jdbcClient.getConnection(res -> {
            if (res.succeeded()) {

                // Transaction must use a connection
                PgConnection conn = res.result();

                // Begin the transaction
                PgTransaction tx = conn.begin();

                for (SqlAndParams sqlParam : sqlAndParamsList) {
                    SqlSession sqlSession = getRealSql(sqlParam.getSqlKey(), sqlParam.getParams());
                    conn.preparedQuery(sqlSession.getSql(), jsonArray2Tuple(sqlSession.getParams()), ar -> {
                        // Works fine of course
                        if (!ar.succeeded()) {
                            tx.rollback();
                            conn.close();
                        }
                    });
                }

                // Commit the transaction
                tx.commit(ar -> {
                    if (ar.succeeded()) {
                        msg.reply(sqlAndParamsList.size());
                    } else {
                        log.error("unknown error", ar.cause().getMessage());
                        tx.rollback();
                        msg.fail(501, ar.cause().getMessage());
                    }

                    // Return the connection to the pool
                    conn.close();
                });
            }
        });

    }

    @Override
    protected JsonObject getDbConfig() {
        Map<String, Object> configs = Properties.getGroup("jdbc");
        return new JsonObject(configs);
    }

    protected JsonArray under2Camel(AsyncResult<PgRowSet> getData) {
        JsonArray array = new JsonArray();
        PgRowSet rs = getData.result();
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
