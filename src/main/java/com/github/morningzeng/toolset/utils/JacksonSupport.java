package com.github.morningzeng.toolset.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.lang.reflect.Type;

public interface JacksonSupport {

    static JacksonSupport withMapper(ObjectMapper mapper) {
        return new JacksonSupport() {
            @SneakyThrows
            @Override
            public <T> String toJson(final T data) {
                return mapper.writeValueAsString(data);
            }

            @SneakyThrows
            @Override
            public <T> String toPrettyJson(final T data) {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            }

            @SneakyThrows
            @Override
            public <T> byte[] toBytes(final T data) {
                return mapper.writeValueAsBytes(data);
            }

            @SneakyThrows
            @Override
            public <T> T fromJson(final String json, final Class<T> tClass) {
                return mapper.readValue(json, tClass);
            }

            @SneakyThrows
            @Override
            public <T> T fromJson(final String json, final Type type) {
                return mapper.readValue(json, mapper.getTypeFactory().constructType(type));
            }

            @SneakyThrows
            @Override
            public <T> T fromJson(final String json, final TypeReference<T> typeReference) {
                return mapper.readValue(json, typeReference);
            }

            @SneakyThrows
            @Override
            public JsonNode fromJson(final String json) {
                return mapper.readTree(json);
            }
        };
    }

    /**
     * Serialize to Json
     * <pre>
     *     Expand the Json display
     *     {@code return OBJECT_MAPPER_MAP.get(this).writerWithDefaultPrettyPrinter().writeValueAsString(data);}
     * </pre>
     *
     * @param data {@link T}
     * @return {@link String}
     */
    <T> String toJson(T data);

    <T> String toPrettyJson(T data);

    <T> byte[] toBytes(T data);

    <T> T fromJson(String json, Class<T> tClass);

    <T> T fromJson(String json, Type type);

    <T> T fromJson(String json, TypeReference<T> typeReference);

    JsonNode fromJson(String json);
}