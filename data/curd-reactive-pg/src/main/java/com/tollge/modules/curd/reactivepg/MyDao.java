package com.tollge.modules.curd.reactivepg;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;

@Slf4j
public class MyDao {
    private MyDao() {
    }

    private static Random r = new Random();

    public static DaoVerticle getDao() {
        if (Singleton.INSTANCE.getInstance().connList.isEmpty()) {
            int i = 0;
            // 防止调用比连接来得早
            while (i < 10) {
                if (!Singleton.INSTANCE.getInstance().connList.isEmpty()) {
                    return Singleton.INSTANCE.getInstance()
                            .connList.get(r.nextInt(Singleton.INSTANCE.getInstance().connList.size()));
                }
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                    log.error("sleep error", e);
                }
                i++;
            }
        }
        return Singleton.INSTANCE.getInstance()
                .connList.get(r.nextInt(Singleton.INSTANCE.getInstance().connList.size()));
    }

    static void init(DaoVerticle conn) {
        Singleton.INSTANCE.init(conn);
    }

    private enum Singleton {
        // 单例
        INSTANCE;

        private MyDao single;

        private Singleton() {
            single = new MyDao();
        }

        public MyDao getInstance() {
            return single;
        }

        void init(DaoVerticle conn) {
            if (!single.connList.contains(conn)) {
                single.connList.add(conn);
            }
        }
    }

    private List<DaoVerticle> connList = Lists.newArrayList();

}