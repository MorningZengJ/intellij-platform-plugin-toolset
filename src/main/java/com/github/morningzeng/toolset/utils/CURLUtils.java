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

    public static HttpBean from(final String cUrl) {
        return HttpBean.builder()
                .request(resolve(cUrl))
                .build();
    }

    static RequestBean resolve(final String cUrl) {
        return RequestBean.builder()
                .method(method(cUrl))
                .header(header(cUrl))
                .url(url(cUrl))
                .body(body(cUrl))
                .build();
    }

    static UrlBean url(final String cUrl) {
        final Pattern compile = Pattern.compile("(?i)(?<=curl)( +?--location)? +?[\"'].+(?=[\"'] )");
        String location = getByPattern(compile, cUrl).replaceFirst("^--location", "");
        final String url = location.trim().substring(1);
        final String[] protocolSplit = url.split("(?i)//:");

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
                        return PairWithTypeDescription.with(split[0], split[1]);
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
        final Pattern compile = Pattern.compile("(?i)(?<=(-H)|(--header)) +?[\"'].+(?=[\"'] )");
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
        }
        if (cUrl.contains("--form") || cUrl.matches("((?i)(?<=-F)) +?[\"']")) {
            mode = HttpBodyTypeEnum.FORM_DATA;
            builder.formData(formData(cUrl));
        }
        if (cUrl.contains("--data")) {
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
        throw new IllegalArgumentException("Invalid cURL: " + cUrl);
    }

    static List<String> host(final String hostname) {
        return Lists.newArrayList(hostname.split("\\."));
    }

    static List<PairWithTypeDescription> urlencoded(final String cUrl) {
        final Pattern compile = Pattern.compile("(?i)(?<=--data-urlencode) +?[\"'].+(?=[\"'])");
        final Matcher matcher = compile.matcher(cUrl);
        final List<PairWithTypeDescription> result = Lists.newArrayList();
        while (matcher.find()) {
            final String[] group = matcher.group().split("=");
            result.add(PairWithTypeDescription.with(group[0], group[1]));
        }
        return result;
    }

    static List<FormData> formData(final String cUrl) {
        final Pattern compile = Pattern.compile("(?i)(?<=(--form)|(-F)) +?[\"'].+(?=[\"'])");
        final Matcher matcher = compile.matcher(cUrl);
        final List<FormData> result = Lists.newArrayList();
        final FormDataBuilder<?, ?> builder = FormData.builder();
        while (matcher.find()) {
            final String[] group = matcher.group().split("=");
            final String value = group[1];
            builder.key(group[0]).value(value).type(HttpBodyParamTypeEnum.TEXT.key());
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
            final String trim = matcher.group().trim();
            return trim.substring(1);
        }
        return null;
    }

}
