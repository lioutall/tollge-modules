import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import io.netty.util.internal.StringUtil;

public class TestApollo {
    public static void main(String[] args) {
        //启动参数添加 -Denv=LPT
        System.setProperty("apollo.cluster", "test1");
        
        //config instance is singleton for each namespace and is never null
        Config config = ConfigService.getConfig("component");

        String someKey = "testKey";
        String someDefaultValue = "nothing";
        String value = config.getProperty(someKey, someDefaultValue);
        System.out.println(value);


        Config config1 = ConfigService.getConfig("application");
        someKey = "testKey";
        someDefaultValue = "nothing";
        value = config1.getProperty(someKey, someDefaultValue);
        System.out.println(value);
    }

}
