package com.github.morningzeng.toolset.enums;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.csv.CsvFactory;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsFactory;
import com.fasterxml.jackson.dataformat.toml.TomlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import com.github.morningzeng.toolset.Constants;
import com.github.morningzeng.toolset.utils.JacksonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public enum OutputType {
    YAML("yaml", new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER)),
    JSON("json", new JsonFactory()),
    XML("xml", new XmlFactory()),
    PROPERTIES("properties", new JavaPropsFactory()),
    CSV("csv", new CsvFactory()),
    TOML("toml", new TomlFactory()),
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
        JacksonUtils.IGNORE_TRANSIENT_AND_NULL.initialize(objectMapper);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
    }

    ObjectMapper mapper() {
        return OBJECT_MAPPERS.get(this);
    }

    public String fullName(final String filename) {
        return String.join(Constants.DOT, filename, this.suffix);
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