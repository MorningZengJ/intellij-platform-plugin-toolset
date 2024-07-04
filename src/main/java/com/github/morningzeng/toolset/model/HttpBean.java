package com.github.morningzeng.toolset.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.github.morningzeng.toolset.Constants;
import com.github.morningzeng.toolset.enums.EnumSupport;
import com.github.morningzeng.toolset.enums.HttpBodyParamTypeEnum;
import com.github.morningzeng.toolset.enums.HttpBodyTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Morning Zeng
 * @since 2024-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class HttpBean extends Children<HttpBean> {
    private String name;
    private RequestBean request;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestBean {
        private String method;
        private List<Pair<String, String>> header;
        private UrlBean url;
        private BodyBean body;
        private String description;

        public String headerText() {
            return this.header.stream()
                    .map(pair -> String.join(Constants.COLON_WITH_SPACE, pair.key, pair.value))
                    .collect(Collectors.joining(System.lineSeparator()));
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UrlBean {
        private String raw;
        private String protocol;
        private List<String> host;
        private List<String> path;
        private List<PairWithTypeDescription> query;

        @Override
        public String toString() {
            return this.raw;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BodyBean {
        private String mode;
        private String raw;
        private List<PairWithTypeDescription> urlencoded;
        @JsonAlias("formdata")
        private List<FormData> formData;

        public final HttpBodyTypeEnum mode() {
            return EnumSupport.get(HttpBodyTypeEnum.class).get(this.mode);
        }

        public final String bodyText() {
            return switch (this.mode()) {
                case RAW -> this.raw;
                case FORM_DATA ->
                        this.formData.stream().map(PairWithTypeDescription::dataText).collect(Collectors.joining(System.lineSeparator()));
                case X_WWW_FORM_URLENCODED ->
                        this.urlencoded.stream().map(PairWithTypeDescription::dataText).collect(Collectors.joining(System.lineSeparator()));
                default -> null;
            };
        }
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class FormData extends PairWithTypeDescription {
        private String src;

    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public non-sealed static class PairWithTypeDescription extends Pair<String, String> {
        @Builder.Default
        private final String type = HttpBodyParamTypeEnum.TEXT.key();
        private String description;

        public static PairWithTypeDescription with(final String key, final String value) {
            return PairWithTypeDescription.builder()
                    .key(key)
                    .value(value)
                    .type(HttpBodyParamTypeEnum.TEXT.key())
                    .build();
        }

        public HttpBodyParamTypeEnum type() {
            return EnumSupport.get(HttpBodyParamTypeEnum.class).get(this.type);
        }

        public PairWithTypeDescription description(final String description) {
            this.description = description;
            return this;
        }

        public String dataText() {
            return String.join(Constants.COLON_WITH_SPACE, this.key, this.value);
        }
    }
}
