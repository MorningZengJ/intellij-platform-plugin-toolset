package com.github.morningzeng.toolset.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.github.morningzeng.toolset.Constants;
import com.github.morningzeng.toolset.Constants.IconC.HttpMethod;
import com.github.morningzeng.toolset.enums.EnumSupport;
import com.github.morningzeng.toolset.enums.HttpBodyParamTypeEnum;
import com.github.morningzeng.toolset.enums.HttpBodyTypeEnum;
import com.github.morningzeng.toolset.utils.CURLUtils;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.net.HTTPMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.apache.commons.compress.utils.Lists;

import javax.swing.Icon;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Morning Zeng
 * @since 2024-07-01
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public final class HttpBean extends Children<HttpBean> {
    private String name;
    private RequestBean request;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestBean {
        @Builder.Default
        private final UrlBean url = new UrlBean();
        @Builder.Default
        private final List<Pair<String, String>> header = Lists.newArrayList();
        @Builder.Default
        private final BodyBean body = new BodyBean();
        private String method;
        private String description;

        public String headerText() {
            return Optional.ofNullable(this.header)
                    .map(pairs -> pairs.stream()
                            .map(pair -> String.join(Constants.COLON_WITH_SPACE, pair.key, pair.value))
                            .collect(Collectors.joining(System.lineSeparator())))
                    .orElse("");
        }

        public void url(String url) {
            if (StringUtil.isEmpty(url)) {
                return;
            }
            url = "curl '%s' ".formatted(url);
            final UrlBean urlBean = CURLUtils.url(url);
            this.url.setRaw(urlBean.getRaw())
                    .setProtocol(urlBean.getProtocol());
            this.url.host.clear();
            this.url.path.clear();
            this.url.query.clear();
            this.url.host.addAll(urlBean.host);
            this.url.path.addAll(urlBean.path);
            this.url.query.addAll(urlBean.query);
        }

        public HTTPMethod method() {
            return Optional.ofNullable(this.method)
                    .map(HTTPMethod::valueOf)
                    .orElse(HTTPMethod.GET);
        }

        public Icon methodIcon() {
            return switch (this.method()) {
                case GET -> HttpMethod.GET;
                case POST -> HttpMethod.POST;
                case PUT -> HttpMethod.PUT;
                case PATCH -> HttpMethod.PATCH;
                case DELETE -> HttpMethod.HTTP_DELETE;
                case HEAD -> HttpMethod.HEAD;
                case OPTIONS -> HttpMethod.OPTIONS;
                case TRACE -> HttpMethod.TRACE;
            };
        }

        public String cURL() {
            final String delimiter = " \\" + System.lineSeparator();

            String cURL = String.join(" ", "curl", "-X", this.method().name(), "--location", "'" + this.url.raw + "'");
            final String header = this.header.stream()
                    .map(pair -> "-H ".concat("'" + String.join(Constants.COLON_WITH_SPACE, pair.key, pair.value) + "'"))
                    .collect(Collectors.joining(delimiter));
            if (StringUtil.isNotEmpty(header)) {
                cURL = String.join(delimiter, cURL, header);
            }
            final String body = this.body.cURL();
            if (StringUtil.isNotEmpty(body)) {
                cURL = String.join(delimiter, cURL, body);
            }
            return cURL;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UrlBean {
        @Builder.Default
        private final List<String> host = Lists.newArrayList();
        @Builder.Default
        private final List<String> path = Lists.newArrayList();
        @Builder.Default
        private final List<PairWithTypeDescription> query = Lists.newArrayList();
        @Builder.Default
        private String raw = "";
        @Builder.Default
        private String protocol = "http";

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
        @Builder.Default
        private final List<PairWithTypeDescription> urlencoded = Lists.newArrayList();
        @JsonAlias("formdata")
        @Builder.Default
        private final List<FormData> formData = Lists.newArrayList();
        @Builder.Default
        private String mode = HttpBodyTypeEnum.NONE.key();
        private String raw;

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

        public final void bodyText(final String text) {
            switch (this.mode()) {
                case RAW -> this.raw = text;
                case FORM_DATA -> {
                    this.formData.clear();
                    this.formData.addAll(
                            text.lines()
                                    .<FormData>map(s -> {
                                        final String[] split = s.split(Constants.COLON_WITH_SPACE);
                                        return FormData.builder()
                                                .key(split[0])
                                                .value(split[1])
                                                .build();
                                    })
                                    .toList()
                    );
                }
                case X_WWW_FORM_URLENCODED -> {
                    this.urlencoded.clear();
                    this.urlencoded.addAll(
                            text.lines()
                                    .map(s -> {
                                        final String[] split = s.split(Constants.COLON_WITH_SPACE);
                                        return PairWithTypeDescription.with(split[0], split[1]);
                                    })
                                    .toList()
                    );
                }
                default -> {
                }
            }
        }

        public String cURL() {
            return switch (this.mode()) {
                case RAW -> String.join(" ", "--data-raw", "'" + this.raw + "'");
                case FORM_DATA -> this.formData.stream()
                        .map(fd -> String.join(" ", "--form", fd.keyValuePair()))
                        .collect(Collectors.joining(" \\" + System.lineSeparator()));
                case X_WWW_FORM_URLENCODED -> this.urlencoded.stream()
                        .map(pair -> String.join(" ", "--data-urlencode", "'%s=%s'".formatted(pair.key, pair.value)))
                        .collect(Collectors.joining(" \\" + System.lineSeparator()));
                default -> "";
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

        public String keyValuePair() {
            return switch (this.type()) {
                case TEXT -> "'%s=%s'".formatted(this.key, this.value);
                case FILE -> "'%s=@\"%s\"'".formatted(this.key, this.value);
            };
        }
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public non-sealed static class PairWithTypeDescription extends Pair<String, String> {
        @Default
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
