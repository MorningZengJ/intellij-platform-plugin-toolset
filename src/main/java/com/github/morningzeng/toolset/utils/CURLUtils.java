package com.github.morningzeng.toolset.utils;

import com.github.morningzeng.toolset.enums.HttpBodyParamTypeEnum;
import com.github.morningzeng.toolset.enums.HttpBodyTypeEnum;
import com.github.morningzeng.toolset.model.HttpBean;
import com.github.morningzeng.toolset.model.HttpBean.BodyBean;
import com.github.morningzeng.toolset.model.HttpBean.BodyBean.BodyBeanBuilder;
import com.github.morningzeng.toolset.model.HttpBean.FormData;
import com.github.morningzeng.toolset.model.HttpBean.FormData.FormDataBuilder;
import com.github.morningzeng.toolset.model.HttpBean.PairWithTypeDescription;
import com.github.morningzeng.toolset.model.HttpBean.RequestBean;
import com.github.morningzeng.toolset.model.HttpBean.UrlBean;
import com.github.morningzeng.toolset.model.Pair;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Morning Zeng
 * @since 2024-07-01
 */
public final class CURLUtils {

    public static String cURL(final HttpBean bean) {
        return Optional.ofNullable(bean.getRequest())
                .map(RequestBean::cURL)
                .orElse("");
    }

    public static HttpBean from(final String cUrl) {
        final HttpBean bean = HttpBean.builder()
                .request(resolve(cUrl))
                .build();
        return bean.setName(String.valueOf(bean.getRequest().getUrl()));
    }

    static RequestBean resolve(final String cUrl) {
        return RequestBean.builder()
                .method(method(cUrl))
                .header(header(cUrl))
                .url(url(cUrl))
                .body(body(cUrl))
                .build();
    }

    public static UrlBean url(final String cUrl) {
        final Pattern compile = Pattern.compile("(?i)((?<=curl)( +?--location.*(?= .+?[\"']))? +?[\"'].+?(?=[\"'] ?))|((?<=--location) +?[\"'].+?(?=[\"'] ?))");
        final String[] locationSpace = getByPattern(compile, cUrl).split(" ");
        String location = locationSpace[locationSpace.length - 1];
        final String url = location.trim().substring(1);
        final String[] protocolSplit = url.split("(?i)://");

        String protocol = "http";
        String hostUrl = url;
        if (protocolSplit.length == 2) {
            protocol = protocolSplit[0];
            hostUrl = protocolSplit[1];
        }

        final String[] hostSplit = hostUrl.split("/");
        String queryPath = hostUrl.substring(hostUrl.indexOf("?") + 1);
        List<String> path = Collections.emptyList();
        if (hostSplit.length > 1) {
            path = Arrays.stream(hostSplit).skip(1)
                    .map(p -> p.split("\\?")[0])
                    .toList();
        }

        List<PairWithTypeDescription> query = Collections.emptyList();
        if (queryPath.contains("&")) {
            query = Arrays.stream(queryPath.split("&"))
                    .map(keyValuePair -> {
                        final String[] split = keyValuePair.split("=");
                        return PairWithTypeDescription.with(split[0], split.length > 1 ? split[1] : null);
                    })
                    .toList();
        }

        return UrlBean.builder()
                .raw(url)
                .protocol(protocol)
                .host(host(hostSplit[0]))
                .path(path)
                .query(query)
                .build();
    }

    static String method(final String cUrl) {
        final Pattern compile = Pattern.compile("(?i)(?<=(-X)|(--request)) +?[\"']?.+(?=[\"']? )");
        return Optional.ofNullable(getByPattern(compile, cUrl, true))
                .map(s -> {
                    if (s.contains(" ")) {
                        return s.split(" ")[0];
                    }
                    if (s.startsWith("'")) {
                        s = s.substring(1);
                    }
                    if (s.endsWith("'")) {
                        s = s.substring(0, s.length() - 1);
                    }
                    return s.toUpperCase();
                })
                .orElse(null);
    }

    static List<Pair<String, String>> header(final String cUrl) {
        final Pattern compile = Pattern.compile("(?i)(?<=(-H)|(--header)) +?[\"'].+?(?=[\"'] *)");
        final Matcher matcher = compile.matcher(cUrl);
        final List<Pair<String, String>> headers = Lists.newArrayList();
        while (matcher.find()) {
            String group = matcher.group().trim();
            if (group.startsWith("'")) {
                group = group.substring(1);
            }
            final String[] split = group.split(":");
            headers.add(Pair.of(split[0], split[1]));
        }
        return headers;
    }

    static BodyBean body(final String cUrl) {
        final BodyBeanBuilder builder = BodyBean.builder();
        HttpBodyTypeEnum mode = HttpBodyTypeEnum.NONE;

        if (cUrl.contains("--data-urlencode")) {
            mode = HttpBodyTypeEnum.X_WWW_FORM_URLENCODED;
            builder.urlencoded(urlencoded(cUrl));
        } else if (cUrl.contains("--form") || cUrl.matches("((?i)(?<=-F)) +?[\"']")) {
            mode = HttpBodyTypeEnum.FORM_DATA;
            builder.formData(formData(cUrl));
        } else if (cUrl.contains("--data")) {
            mode = HttpBodyTypeEnum.RAW;
            builder.raw(rawBody(cUrl));
        }

        return builder
                .mode(mode.key())
                .build();
    }

    static String getByPattern(final Pattern compile, final String cUrl) {
        return getByPattern(compile, cUrl, false);
    }

    static String getByPattern(final Pattern compile, final String cUrl, final boolean ignore) {
        final Matcher matcher = compile.matcher(cUrl);
        if (matcher.find()) {
            return matcher.group().trim();
        }
        if (ignore) {
            return null;
        }
        throw new IllegalArgumentException("Parsing cURL Lost: " + cUrl);
    }

    static List<String> host(final String hostname) {
        return Lists.newArrayList(hostname.split("\\."));
    }

    static List<PairWithTypeDescription> urlencoded(final String cUrl) {
        final Pattern compile = Pattern.compile("(?i)(?<=--data-urlencode) +?[\"'].+(?=[\"'])");
        final Matcher matcher = compile.matcher(cUrl);
        final List<PairWithTypeDescription> result = Lists.newArrayList();
        while (matcher.find()) {
            String group = matcher.group().trim();
            if (group.startsWith("'")) {
                group = group.substring(1);
            }
            final String[] equalsSign = group.split("=");
            result.add(PairWithTypeDescription.with(equalsSign[0], equalsSign[1]));
        }
        return result;
    }

    static List<FormData> formData(final String cUrl) {
        final Pattern compile = Pattern.compile("(?i)(?<=(--form)|(-F)) +?[\"'].+(?=[\"'])");
        final Matcher matcher = compile.matcher(cUrl);
        final List<FormData> result = Lists.newArrayList();
        final FormDataBuilder<?, ?> builder = FormData.builder();
        while (matcher.find()) {
            String group = matcher.group().trim();
            if (group.startsWith("'")) {
                group = group.substring(1);
            }
            final String[] equalsSign = group.split("=");
            final String value = equalsSign[1];
            builder.key(equalsSign[0]).value(value).type(HttpBodyParamTypeEnum.TEXT.key());
            if (value.startsWith("@")) {
                builder.type(HttpBodyParamTypeEnum.FILE.key());
            }
            result.add(builder.build());
        }
        return result;
    }

    static String rawBody(final String cUrl) {
        final Pattern compile = Pattern.compile("(?i)(?<=--data(-raw)?) +?[\"'].+(?=[\"'])");
        final Matcher matcher = compile.matcher(cUrl);
        if (matcher.find()) {
            String trim = matcher.group().trim();
            if (trim.startsWith("'")) {
                trim = trim.substring(1);
            }
            return trim.substring(1);
        }
        return null;
    }

}
