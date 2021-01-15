package ru.terentev.stepsGenerator.Annotations;

import java.lang.annotation.*;

@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface RouterKeeper {
}
