package com.github.morningzeng.toolset.enums;

import com.fasterxml.jackson.databind.JsonNode;
import com.intellij.openapi.util.text.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.github.morningzeng.toolset.utils.JacksonUtils.IGNORE_TRANSIENT_AND_NULL;

/**
 * @author Morning Zeng
 * @since 2024-05-24
 */
@Getter
@AllArgsConstructor
public enum DataFormatTypeEnum {

    TEXT {
        @Override
        public String output(final String str) {
            final JsonNode o = IGNORE_TRANSIENT_AND_NULL.fromJson(str);
            return IGNORE_TRANSIENT_AND_NULL.toJson(o);
        }
    },
    JSON {
        @Override
        public String output(final String str) {
            final JsonNode o = IGNORE_TRANSIENT_AND_NULL.fromJson(str);
            return IGNORE_TRANSIENT_AND_NULL.toPrettyJson(o);
        }
    },

    ;

    abstract String output(final String str);

    public String out(final String str) {
        if (StringUtil.isEmpty(str)) {
            return "";
        }
        try {
            return this.output(str);
        } catch (Exception e) {
            return str;
        }
    }
}
