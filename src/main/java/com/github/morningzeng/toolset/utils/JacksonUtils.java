package com.github.morningzeng.toolset.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.YearMonthDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.YearMonthSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Jackson工具类
 * <pre>
 *     {@code {
 *      JacksonUtils.NORMAL.toJson(student);
 *      JacksonUtils.UNDER.toJson(student);
 *      JacksonUtils.IGNORE_TRANSIENT.fromJson("{}", Student.class);
 *      ...
 * }}
 * </pre>
 *
 * @author Morning Zeng
 * @since 2024-01-23 15:41:56
 */
@Getter
@AllArgsConstructor
public enum JacksonUtils implements JacksonSupport {
    /**
     * 常规
     */
    NORMAL(1, "常规"),
    /**
     * 下划线/驼峰
     */
    UNDER(2, "下划线/驼峰") {
        @Override
        void initialize(ObjectMapper objectMapper) {
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            super.initialize(objectMapper);
        }
    },
    /**
     * 忽略transient关键字
     */
    IGNORE_TRANSIENT(3, "忽略transient关键字") {
        @Override
        void initialize(ObjectMapper objectMapper) {
            objectMapper.setVisibility(
                    objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                            .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                            .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                            .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
            );
            super.initialize(objectMapper);
        }
    },
    /**
     * 忽略未定义字段
     */
    IGNORE_UNKNOWN(4, "忽略未定义字段") {
        @Override
        void initialize(ObjectMapper objectMapper) {
            objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            super.initialize(objectMapper);
        }
    },
    /**
     * 忽略null值
     */
    IGNORE_NULL(5, "忽略null值") {
        @Override
        void initialize(ObjectMapper objectMapper) {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            super.initialize(objectMapper);
        }
    },
    /**
     * key：null->"null"
     */
    KEY_NULL(6, "key为null") {
        @Override
        void initialize(ObjectMapper objectMapper) {
            objectMapper.getSerializerProvider().setNullKeySerializer(new JsonSerializer<>() {
                @Override
                public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    gen.writeFieldName("null");
                }
            });
            super.initialize(objectMapper);
        }
    },
    /**
     * 输出 忽略transient关键字、null
     */
    IGNORE_TRANSIENT_AND_NULL(11, "输出 忽略transient关键字、null") {
        @Override
        void initialize(ObjectMapper objectMapper) {
            IGNORE_TRANSIENT.initialize(objectMapper);
            IGNORE_NULL.initialize(objectMapper);
            super.initialize(objectMapper);
        }
    },

    ;

    /**
     * 枚举对应的ObjectMapper实例
     */
    private final static Map<JacksonUtils, ObjectMapper> OBJECT_MAPPER_MAP = Arrays.stream(JacksonUtils.values()).collect(
            Collectors.toUnmodifiableMap(Function.identity(), e -> {
                final ObjectMapper objectMapper = new ObjectMapper();
                e.initialize(objectMapper);
                return objectMapper;
            })
    );

    private final int code;
    private final String desc;

    /**
     * 初始化操作，需要对ObjectMapper实例的配置
     *
     * @param objectMapper {@link ObjectMapper}
     */
    void initialize(ObjectMapper objectMapper) {
        final JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .addSerializer(YearMonth.class, new YearMonthSerializer(DateTimeFormatter.ofPattern("yyyy-MM")))
                .addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .addDeserializer(YearMonth.class, new YearMonthDeserializer(DateTimeFormatter.ofPattern("yyyy-MM")))
                .addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        objectMapper.registerModule(javaTimeModule);
    }

    ObjectMapper mapper() {
        return OBJECT_MAPPER_MAP.get(this);
    }

    ObjectMapper copy(Consumer<ObjectMapper> consumer) {
        final ObjectMapper copy = OBJECT_MAPPER_MAP.get(this).copy();
        this.initialize(copy);
        consumer.accept(copy);
        return copy;
    }

    public JacksonSupport support(Consumer<ObjectMapper> consumer) {
        return JacksonSupport.withMapper(this.copy(consumer));
    }

    @Override
    public <T> String toJson(final T data) {
        return JacksonSupport.withMapper(this.mapper()).toJson(data);
    }

    @Override
    public <T> String toPrettyJson(final T data) {
        return JacksonSupport.withMapper(this.mapper()).toPrettyJson(data);
    }

    @Override
    public <T> byte[] toBytes(final T data) {
        return JacksonSupport.withMapper(this.mapper()).toBytes(data);
    }

    @Override
    public <T> T fromJson(final String json, final Class<T> tClass) {
        return JacksonSupport.withMapper(this.mapper()).fromJson(json, tClass);
    }

    @Override
    public <T> T fromJson(final String json, final Type type) {
        return JacksonSupport.withMapper(this.mapper()).fromJson(json, type);
    }

    @Override
    public <T> T fromJson(final String json, final TypeReference<T> typeReference) {
        return JacksonSupport.withMapper(this.mapper()).fromJson(json, typeReference);
    }

    @Override
    public JsonNode fromJson(final String json) {
        return JacksonSupport.withMapper(this.mapper()).fromJson(json);
    }
}

