package by.rublevskaya.task4.minispring.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Scope {
    ScopeType value() default ScopeType.SINGLETON;
}
