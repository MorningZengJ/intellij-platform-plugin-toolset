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
public enum HttpBodyTypeEnum implements EnumSupport<String> {

    NONE("none"),
    FORM_DATA("formdata"),
    X_WWW_FORM_URLENCODED("urlencoded"),
    RAW("raw"),
    BINARY("binary"),
    GRAPH_QL("GraphQL"),
    ;

    private final String key;

}
