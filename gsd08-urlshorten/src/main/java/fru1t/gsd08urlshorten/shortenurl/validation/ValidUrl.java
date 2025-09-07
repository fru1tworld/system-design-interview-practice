package fru1t.gsd08urlshorten.shortenurl.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UrlValidator.class)
@Documented
public @interface ValidUrl {
    String message() default "잘못된 URL 형식입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}