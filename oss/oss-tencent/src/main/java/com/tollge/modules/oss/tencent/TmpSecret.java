package com.tollge.modules.oss.tencent;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class TmpSecret {

    private String tmpSecretId;
    private String tmpSecretKey;
    private String sessionToken;
}
