package test.curd;

import com.tollge.common.BaseModel;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class TestDo extends BaseModel {
    private String a;
    private String underlineToCamel;
}
