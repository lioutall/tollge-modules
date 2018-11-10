package com.tollge.modules.web.http2;

import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Http2 {
    String value() default "";
}
