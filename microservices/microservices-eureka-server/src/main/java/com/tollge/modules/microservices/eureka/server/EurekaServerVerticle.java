package com.tollge.modules.microservices.eureka.server;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClient;
import com.tollge.modules.microservices.eureka.TollgeEurekaInstanceConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

/**
 * eureka服务(Provider)Verticle
 *
 * @author toyer
 */
@Slf4j
public class EurekaServerVerticle extends AbstractVerticle {
    private EurekaClient eurekaClient;
    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        // 注册eureka服务
        EurekaInstanceConfig instanceConfig = new TollgeEurekaInstanceConfig();
        InstanceInfo instanceInfo = new EurekaConfigBasedInstanceInfoProvider(instanceConfig).get();
        ApplicationInfoManager applicationInfoManager = new ApplicationInfoManager(instanceConfig, instanceInfo);

        eurekaClient = new DiscoveryClient(applicationInfoManager, new DefaultEurekaClientConfig());

        applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.STARTING);

        applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.UP);
    }

    @Override
    public void stop() throws Exception {
        if (eurekaClient != null) {
            log.info("Shutting down EurekaServerVerticle...");
            eurekaClient.shutdown();
        }
        super.stop();
    }

}
