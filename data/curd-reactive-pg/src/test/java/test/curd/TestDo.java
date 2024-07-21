package test.curd;

import com.tollge.common.BaseDo;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class TestDo extends BaseDo {
    private String a;
    private String underlineToCamel;
}
