package com.tollge.modules.curd.reactivepg;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import com.tollge.common.OperationResult;
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
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.impl.ArrayTuple;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tollge.common.simple.Handle.fromHandler;
import static com.tollge.common.util.Const.RETURN_CLASS_TYPE;

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
        // Create the pooled client
        jdbcClient = PgBuilder.pool().with(new PoolOptions())
                .connectingTo(new PgConnectOptions(getDbConfig())).using(vertx).build();
        MyDao.init(this);
    }

    public Future<Long> count(SqlAndParams sqlAndParams) {
        return Future.<SqlConnection>future(reply -> jdbcClient.getConnection(fromHandler(reply))
        ).compose(conn -> {
            SqlSession sqlSession = getSqlSession(sqlAndParams);
            return Future.<RowSet<Row>>future(reply -> conn.preparedQuery(sqlSession.getSql())
                            .execute(jsonArray2Tuple(sqlSession.getParams()), fromHandler(reply)))
                    .compose(res -> {
                        RowIterator<Row> ite = res.iterator();
                        Row r = ite.next();
                        return Future.succeededFuture(r.getLong(0));
                    }).onComplete(o -> conn.close());
        });
    }

    @Override
    protected void count(Message<JsonObject> msg) {
        SqlAndParams sqlAndParams = fetchSqlAndParams(msg.body());
        count(sqlAndParams)
                .onSuccess(msg::reply)
                .onFailure(e -> msg.fail(501, errorMsg(sqlAndParams.getSqlKey(), e)));
    }

    private SqlSession getSqlSession(SqlAndParams sqlAndParams) {
        SqlSession r = getRealSql(sqlAndParams.getSqlKey(), sqlAndParams.getParams());
        StringBuilder sb = new StringBuilder();
        int j = 1;
        for (char b : r.getSql().toCharArray()) {
            if (b == '?') {
                sb.append('$');
                sb.append(j++);
            } else {
                sb.append(b);
            }
        }
        r.setSql(sb.toString());
        return r;
    }

    private SqlSession getSqlSession(Message<?> msg, SqlAndParams sqlAndParams) {
        try {
            SqlSession r = getRealSql(sqlAndParams.getSqlKey(), sqlAndParams.getParams());
            StringBuilder sb = new StringBuilder();
            int j = 1;
            for (char b : r.getSql().toCharArray()) {
                if (b == '?') {
                    sb.append('$');
                    sb.append(j++);
                } else {
                    sb.append(b);
                }
            }
            r.setSql(sb.toString());
            return r;
        } catch (Exception e) {
            log.error(GET_REAL_SQL_FAILED + sqlAndParams.getSqlKey(), e);
            msg.fail(501, e.toString());
            return null;
        }
    }

    public <T> Future<Page<T>> page(SqlAndParams sqlAndParams, Class<T> cls) {
        if (sqlAndParams.getLimit() == 0) {
            throw new SqlEngineException("limit should not be 0");
        }

        return Future.<SqlConnection>future(reply -> jdbcClient.getConnection(fromHandler(reply))
        ).compose(conn -> {
            SqlSession sqlSession = getSqlSession(sqlAndParams);
            return Future.all(Future.<RowSet<Row>>future(reply -> conn.preparedQuery("select count(1) from (".concat(sqlSession.getSql()).concat(") tab"))
                            .execute(jsonArray2Tuple(sqlSession.getParams()), fromHandler(reply)))
                    ,
                    Future.<RowSet<Row>>future(reply -> conn.preparedQuery(sqlSession.getSql().concat(" limit " + sqlAndParams.getLimit() + " offset " + sqlAndParams.getOffset()))
                            .execute(jsonArray2Tuple(sqlSession.getParams()), fromHandler(reply)))
                    ,
                    Future.succeededFuture(conn)
            );
        }).compose(res -> {
            SqlConnection o3 = res.resultAt(2);
            o3.close();
            RowSet<Row> o1 = res.resultAt(0);
            RowSet<Row> o2 = res.resultAt(1);

            RowIterator<Row> ite = o1.iterator();
            Row r = ite.next();
            Long count = r.getLong(0);

            Page<T> rPage = new Page<>(sqlAndParams.getOffset() / sqlAndParams.getLimit() + 1, sqlAndParams.getLimit());
            rPage.setTotal(count);

            JsonArray rows = under2Camel(o2);

            for (int i = 0; i < rows.size(); i++) {
              rPage.getResult().add(rows.getJsonObject(i).mapTo(cls));
            }

            return Future.succeededFuture(rPage);
        });
    }

    @Override
    protected void page(Message<JsonObject> msg) {
        final SqlAndParams sqlAndParams = fetchSqlAndParams(msg);

        Class<?> cCls = getClassFromMsg(msg);

        page(fetchSqlAndParams(msg.body()), cCls)
                .onSuccess(msg::reply)
                .onFailure(e -> msg.fail(501, errorMsg(sqlAndParams.getSqlKey(), e)));
    }

    private static Class<?> getClassFromMsg(Message<?> msg) {
        String headerCls = msg.headers().get(RETURN_CLASS_TYPE);
        Class<?> cCls = null;
        try {
            if (!StringUtil.isNullOrEmpty(headerCls)) {
                cCls = Class.forName(headerCls);
            } else {
                cCls = JsonObject.class;
            }
        } catch (ClassNotFoundException e) {
            msg.fail(501, RETURN_CLASS_TYPE + " is not defined, value=" + msg.headers().get(RETURN_CLASS_TYPE));
        }
        return cCls;
    }

    public <T> Future<List<T>> list(SqlAndParams sqlAndParams, Class<T> cls) {
        return Future.<SqlConnection>future(reply -> jdbcClient.getConnection(fromHandler(reply))
        ).compose(conn -> {
            SqlSession sqlSession = getSqlSession(sqlAndParams);
            return Future.<RowSet<Row>>future(reply -> conn.preparedQuery(sqlSession.getSql())
                            .execute(jsonArray2Tuple(sqlSession.getParams()), fromHandler(reply)))
                    .compose(res -> {
                       return Future.succeededFuture(under2Camel(res, cls));
                    }).onComplete(o -> conn.close());
        });
    }

    @Override
    protected void list(Message<JsonObject> msg) {
        final SqlAndParams sqlAndParams = fetchSqlAndParams(msg);
        Class<?> cCls = getClassFromMsg(msg);

        list(sqlAndParams, cCls)
                .onSuccess(msg::reply)
                .onFailure(e -> msg.fail(501, errorMsg(sqlAndParams.getSqlKey(), e)));
    }

    public <T> Future<T> one(SqlAndParams sqlAndParams, Class<T> cls) {
        return Future.<SqlConnection>future(reply -> jdbcClient.getConnection(fromHandler(reply))
        ).compose(conn -> {
            SqlSession sqlSession = getSqlSession(sqlAndParams);
            return Future.<RowSet<Row>>future(reply -> conn.preparedQuery(sqlSession.getSql())
                            .execute(jsonArray2Tuple(sqlSession.getParams()), fromHandler(reply)))
                    .compose(res -> {
                        List<T> ts = under2Camel(res, cls);
                        return Future.succeededFuture(ts.isEmpty() ? null : ts.getFirst());
                    }).onComplete(o -> conn.close());
        });
    }

    @Override
    protected void one(Message<JsonObject> msg) {
        final SqlAndParams sqlAndParams = fetchSqlAndParams(msg);
        Class<?> cCls = getClassFromMsg(msg);

        one(sqlAndParams, cCls)
                .onSuccess(msg::reply)
                .onFailure(e -> msg.fail(501, errorMsg(sqlAndParams.getSqlKey(), e)));
    }

    private static String errorMsg(String key, Throwable e) {
        return "[" + key + "]" + e.getMessage();
    }

    public <T> Future<OperationResult<T>> operate(SqlAndParams sqlAndParams, Class<T> cls) {
        return Future.<SqlConnection>future(reply -> jdbcClient.getConnection(fromHandler(reply))
        ).compose(conn -> {
            SqlSession sqlSession = getSqlSession(sqlAndParams);
            return Future.<RowSet<Row>>future(reply -> conn.preparedQuery(sqlSession.getSql())
                            .execute(jsonArray2Tuple(sqlSession.getParams()), fromHandler(reply)))
                    .compose(res -> {
                        OperationResult<T> result = new OperationResult<>();
                        result.setCountRow(res.rowCount());
                        if (res.columnsNames() != null && !res.columnsNames().isEmpty()) {

                            for (Row r : res) {
                                JsonObject jo = new JsonObject();
                                for (int i = 0; i < res.columnsNames().size(); i++) {
                                    jo.put(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, res.columnsNames().get(i)), r.getValue(i));
                                }

                                result.add(jo.mapTo(cls));
                            }
                        }
                        return Future.succeededFuture(result);
                    }).onComplete(o -> conn.close());
        });
    }

    @Override
    protected void operate(Message<JsonObject> msg) {
        final SqlAndParams sqlAndParams = fetchSqlAndParams(msg);
        Class<?> cCls = getClassFromMsg(msg);

        operate(sqlAndParams, cCls)
                .onSuccess(msg::reply)
                .onFailure(e -> msg.fail(501, errorMsg(sqlAndParams.getSqlKey(), e)));
    }

    public <T> Future<List<T>> batch(SqlAndParams sqlAndParams, Class<T> cCls) {

        List<Tuple> collect = Lists.newArrayList();
        SqlSession sqlSession = SqlTemplate.generateSQL(sqlAndParams.getSqlKey(), sqlAndParams.getBatchParams().getFirst());
        String sqlWithNum = w2n(sqlSession.getSql(), sqlSession.getParams().size());
        collect.add(jsonArray2Tuple(sqlSession.getParams()));
        for (int i = 1; i < sqlAndParams.getBatchParams().size(); i++) {
            SqlSession sqlSessionTmp = SqlTemplate.generateSQL(sqlAndParams.getSqlKey(), sqlAndParams.getBatchParams().get(i));
            collect.add(jsonArray2Tuple(sqlSessionTmp.getParams()));
        }

        return Future.<SqlConnection>future(reply -> jdbcClient.getConnection(fromHandler(reply))
        ).compose(conn -> {
            return conn.preparedQuery(sqlWithNum).executeBatch(collect)
                    .compose(res -> {
                        return Future.succeededFuture(under2Camel(res, cCls));
                    }).onComplete(o -> conn.close());
        });
    }
    private String w2n(String sql, int count) {
        int i = 1;
        int j = 0;
        char[] sqlN = new char[sql.length() + count];
        for (char c : sql.toCharArray()) {
            if(c == '?'){
                sqlN[j++] = '$';
                sqlN[j++] = (char) ('0' + i++);
            } else {
                sqlN[j++] = c;
            }
        }
        return new String(sqlN);
    }

    @Override
    protected void batch(Message<SqlAndParams> msg) {
        final SqlAndParams sqlAndParams = msg.body();
        if (sqlAndParams == null) {
            msg.fail(501, "batch 为空, 请检查");
            return;
        }
        Class<?> cCls = getClassFromMsg(msg);

        batch(sqlAndParams, cCls)
                .onSuccess(msg::reply)
                .onFailure(e -> msg.fail(501, errorMsg(sqlAndParams.getSqlKey(), e)));
    }

    @Override
    public void transaction(Message<JsonArray> msg) {
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

                            Future<RowSet<Row>> updates = Future.succeededFuture();

                            for (SqlAndParams sqlParam : sqlAndParamsList) {
                                final String sql = getRealSqlAndParams(sqlParam.getSqlKey(), sqlParam.getParams());
                                updates = updates.compose(a -> conn.query(sql).execute()
                                ).compose(a -> {
                                    log.debug("执行key[{}] sql[{}]完成 返回行数[{}]", sqlParam.getSqlKey(), sql, a.rowCount());
                                    if ("0".equals(ignore)) {
                                        int result = a.rowCount();
                                        if (result == 0) {
                                            throw new TollgeException("no update: " + sqlParam.getSqlKey());
                                        }
                                    }
                                    return Future.succeededFuture();
                                });
                            }

                    return updates.compose(res -> {
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
                                    return Future.succeededFuture();
                            }
                    );
                })
                // Return the connection to the pool
                .eventually(() -> conn.close())
                .onSuccess(v -> log.info("Transaction succeeded"))
                .onFailure(err -> {log.error("Transaction failed: ", err); msg.fail(501, err.getMessage());});
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

    protected JsonArray under2Camel(RowSet<Row> rs) {
        JsonArray array = new JsonArray();
        List<String> columns = rs.columnsNames();

        for (Row r : rs) {
            JsonObject j = new JsonObject();
            for (String cn : columns) {
                j.put(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, cn), r.getValue(cn));
            }
            array.add(j);
        }
        return array;
    }

    protected JsonArray under2Camel(AsyncResult<RowSet<Row>> getData) {
        RowSet<Row> rs = getData.result();
        return under2Camel(rs);
    }

    protected <T> List<T> under2Camel(RowSet<Row> rs, Class<T> cls) {
        List<T> list = Lists.newArrayList();
        List<String> columns = rs.columnsNames();

        for (RowSet<Row> rows = rs;rows != null;rows = rows.next()) {
            for (Row r : rows) {
                JsonObject j = new JsonObject();
                for (String cn : columns) {
                    j.put(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, cn), safeObj(r.getValue(cn)));
                }
                if (cls == JsonObject.class) {
                    list.add((T)j);
                } else {
                    list.add(j.mapTo(cls));
                }
            }
        }

        return list;
    }

    /**
     * vertx 的jsonObject message codec不支持LocalDateTime
     */
    private static Object safeObj(Object o) {
        String jsonFormat = Properties.getString("application", "json.format", "yyyy-MM-dd HH:mm:ss");
        if (o instanceof LocalDateTime) {
            return ((LocalDateTime) o).format(DateTimeFormatter.ofPattern(jsonFormat));
        } else if (o instanceof LocalDate) {
            return ((LocalDate) o).format(DateTimeFormatter.ofPattern(jsonFormat));
        }
        return o;
    }

    protected <T> List<T> under2Camel(AsyncResult<RowSet<Row>> getData, Class<T> cls) {
        RowSet<Row> rs = getData.result();
        return under2Camel(rs, cls);
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

    protected SqlAndParams fetchSqlAndParams(JsonObject json) {
        SqlAndParams sqlAndParams = json != null ? json.mapTo(SqlAndParams.class) : null;
        if (sqlAndParams == null) {
            throw new SqlEngineException("sqlAndParams is null");
        }
        return sqlAndParams;
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

    protected String getRealSqlAndParams(String sqlKey, Map<String, Object> params) {
        try {
            SqlSession sqlSession = SqlTemplate.generateSQL(sqlKey, params);
            return replaceQuestionMarks(sqlSession.getSql(), sqlSession.getParams());
        } catch (RuntimeException e) {
            log.error("获取sql[{}]异常", sqlKey, e);
            throw e;
        }
    }

    private static String replaceQuestionMarks(String input, List<Object> params) {
        StringBuilder result = new StringBuilder();
        int counter = 0; // 起始数字为0

        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            if (currentChar == '?') {
                Object o = params.get(counter);
                if(o instanceof String) {
                    result.append("'").append(o).append("'"); // 字符串用单引号包裹
                } else if(o instanceof Number) {
                    result.append(o); // 数字直接保留
                } else if(o == null) {
                    result.append("null"); // null用null表示
                } else if(o instanceof Boolean) {
                    result.append(o); // boolean直接保留
                } else if(o instanceof Map) {
                    result.append("'").append(Json.encode(o)).append("'");
                } else if(o instanceof List) {
                    result.append("'").append(Json.encode(o)).append("'");
                }
                else {
                    throw new SqlEngineException("不支持的参数类型:" + o.getClass());
                }
                counter++;            // 数字递增
            } else {
                result.append(currentChar); // 非问号直接保留
            }
        }

        return result.toString();
    }

    private Tuple jsonArray2Tuple(List<Object> list) {
        Tuple t = new ArrayTuple(list.size());
        for (Object elt : list) {
            if(elt instanceof List) {
                t.addValue(new JsonArray((List)elt));
            } else if(elt instanceof Map) {
                t.addValue(new JsonObject((Map) elt));
            } else {
                t.addValue(elt);
            }
        }
        return t;
    }

}
