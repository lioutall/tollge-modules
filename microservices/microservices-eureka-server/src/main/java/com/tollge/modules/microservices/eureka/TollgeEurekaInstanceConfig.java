package com.tollge.modules.microservices.eureka;

import com.netflix.appinfo.DataCenterInfo;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.PropertiesInstanceConfig;
import com.tollge.common.util.Properties;
import io.netty.util.internal.StringUtil;

import javax.inject.Singleton;

@Singleton
public class TollgeEurekaInstanceConfig extends PropertiesInstanceConfig implements EurekaInstanceConfig {

    public TollgeEurekaInstanceConfig() {
    }

    public TollgeEurekaInstanceConfig(String namespace) {
        super(namespace);
    }

    public TollgeEurekaInstanceConfig(String namespace, DataCenterInfo dataCenterInfo) {
        super(namespace, dataCenterInfo);
    }

    private EurekaUtil util = new EurekaUtil();
    @Override
    public String getIpAddress() {
        return util.getInetAddress().getHostAddress();
    }

    @Override
    public int getNonSecurePort() {
        return super.getNonSecurePort() == 80 ? Properties.getInteger("application", "http.port"):super.getNonSecurePort();
    }

    @Override
    public String getInstanceId() {
        String instanceId = super.getInstanceId();
        if(StringUtil.isNullOrEmpty(instanceId)) {
            return this.getIpAddress() + ":" + this.getNonSecurePort();
        }
        return instanceId;
    }
}
