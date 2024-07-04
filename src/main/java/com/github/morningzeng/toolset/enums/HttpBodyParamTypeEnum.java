package com.github.morningzeng.toolset.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * @author Morning Zeng
 * @since 2024-07-01
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public enum HttpBodyParamTypeEnum implements EnumSupport<String> {

    TEXT("text"),
    FILE("file"),

    ;

    private final String key;
}
