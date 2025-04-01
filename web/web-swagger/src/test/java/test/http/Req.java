package test.http;

import com.tollge.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(title = "Req", description = "Req描述")
public class Req extends PageRequest {

    @Schema(title = "name1", maxLength = 20, required = true, description = "name描述")
    private String name;

}
