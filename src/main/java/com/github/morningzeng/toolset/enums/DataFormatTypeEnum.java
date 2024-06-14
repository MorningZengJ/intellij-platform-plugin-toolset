package com.github.morningzeng.toolset.enums;

import com.fasterxml.jackson.databind.JsonNode;
import com.intellij.json.json5.Json5FileType;
import com.intellij.json.json5.Json5Language;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
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

    TEXT(PlainTextFileType.INSTANCE, PlainTextLanguage.INSTANCE) {
        @Override
        public String output(final String str) {
            return StringUtil.convertLineSeparators(str);
        }
    },
    JSON(Json5FileType.INSTANCE, Json5Language.INSTANCE) {
        @Override
        public String output(final String str) {
            final JsonNode o = IGNORE_TRANSIENT_AND_NULL.fromJson(str);
            return IGNORE_TRANSIENT_AND_NULL.toPrettyJson(o);
        }
    },

    ;

    private final FileType fileType;
    private final Language language;

    public static DataFormatTypeEnum fileType(final String content) {
        for (final DataFormatTypeEnum e : DataFormatTypeEnum.values()) {
            if (e == TEXT) {
                continue;
            }
            try {
                e.output(content);
                return e;
            } catch (Exception ignore) {
            }
        }
        return TEXT;
    }

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
