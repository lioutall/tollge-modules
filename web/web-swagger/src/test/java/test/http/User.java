package test.http;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Schema(title = "user", description = "User描述")
public class User {

    @Schema(title = "name1", maxLength = 20, required = true, description = "name描述")
    private String name;

    @Schema(title = "年龄", maximum = "100", description = "年龄描述")
    private int age;

    @Schema(title = "出生日期")
    private Date bornDay;

    @Schema(title = "喜欢书籍", description = "喜欢书籍描述")
    private List<String> books;
}
