package com.github.morningzeng.toolset.annotations;

import com.github.morningzeng.toolset.enums.OutputType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Morning Zeng
 * @since 2024-06-28
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScratchConfig {

    String value();

    String directory() default "";

    OutputType outputType() default OutputType.YAML;

}
