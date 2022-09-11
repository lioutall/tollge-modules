package test.http;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Schema(name = "user")
public class User {

    @Schema(name = "name1", title = "name1", maxLength = 20, required = true)
    private String name;

    @Schema(title = "年龄", maximum = "100")
    private int age;

    @Schema(title = "出生日期")
    private Date bornDay;
}
