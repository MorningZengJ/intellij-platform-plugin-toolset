package com.github.morningzeng.toolset.annotations;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Morning Zeng
 * @since 2024-06-28
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScratchConfig {

    String value();

    String directory() default "";

    String filename() default "toolset-config";

    OutputType outputType() default OutputType.YAML;

    @AllArgsConstructor
    enum OutputType {
        YAML("yaml", new YAMLFactory()),
        JSON("json", new JsonFactory()),
        XML("xml", new XmlFactory()),
        ;

        private static final Map<OutputType, ObjectMapper> OBJECT_MAPPERS = Arrays.stream(OutputType.values()).collect(
                Collectors.toUnmodifiableMap(Function.identity(), e -> {
                    final ObjectMapper mapper = new ObjectMapper(e.factory);
                    e.initialize(mapper);
                    return mapper;
                })
        );

        @Getter
        @Accessors(fluent = true)
        private final String suffix;
        private final JsonFactory factory;

        void initialize(final ObjectMapper objectMapper) {
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
        }

        ObjectMapper mapper() {
            return OBJECT_MAPPERS.get(this);
        }

        @SneakyThrows
        public <T> String serialize(final T data) {
            return this.mapper().writeValueAsString(data);
        }

        @SneakyThrows
        public <T> T deserialize(final String content, final Class<T> clazz) {
            return this.mapper().readValue(content, clazz);
        }

        @SneakyThrows
        public <T> T deserialize(final String content, final TypeReference<T> typeReference) {
            return this.mapper().readValue(content, typeReference);
        }

        @SneakyThrows
        public <T> T deserialize(final String content, final Type type) {
            final ObjectMapper mapper = this.mapper();
            return mapper.readValue(content, mapper.getTypeFactory().constructType(type));
        }

    }

}