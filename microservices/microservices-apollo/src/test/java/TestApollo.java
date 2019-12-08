import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;

public class TestApollo {
    public static void main(String[] args) {
        Config config = ConfigService.getAppConfig(); //config instance is singleton for each namespace and is never null
        String someKey = "someKeyFromDefaultNamespace";
        String someDefaultValue = "someDefaultValueForTheKey";
        String value = config.getProperty(someKey, someDefaultValue);
    }

}
