package ru.terentev.stepsGenerator.Annotations;

import java.lang.annotation.*;

@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface PageObject {
    Class<?> baseClass() default PageObject.class;
}
