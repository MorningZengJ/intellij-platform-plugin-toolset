package com.github.morningzeng.toolbox.utils

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.YearMonthDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.YearMonthSerializer
import com.github.morningzeng.toolbox.Constants.DateTimeFormatterC
import java.io.IOException
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth

/**
 * @author Morning Zeng
 * @since 2025-05-16
 */
interface JacksonSupport {

    companion object {
        @JvmStatic
        fun withMapper(mapper: ObjectMapper): JacksonSupport {
            return object : JacksonSupport {
                override fun <T> serialize(data: T?): String? {
                    return mapper.writeValueAsString(data)
                }

                override fun <T> prettySerialize(data: T?): String? {
                    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data)
                }

                override fun <T> bytes(data: T?): ByteArray? {
                    return mapper.writeValueAsBytes(data)
                }

                override fun <T> deserialize(data: String?, clazz: Class<T>): T? {
                    if (data?.isEmpty() == true) return null
                    return mapper.readValue<T?>(data, clazz)
                }

                override fun <T> deserialize(data: String?, type: Type): T? {
                    if (data?.isEmpty() == true) return null
                    return mapper.readValue<T?>(data, mapper.typeFactory.constructType(type))
                }

                override fun <T> deserialize(data: String?, typeReference: TypeReference<T>): T? {
                    if (data?.isEmpty() == true) return null
                    return mapper.readValue<T?>(data, typeReference)
                }

                override fun deserialize(data: String?): JsonNode? {
                    if (data?.isEmpty() == true) return null
                    return mapper.readTree(data)
                }
            }
        }
    }

    /**
     * Serialize to JSON
     * <pre>
     * Expand the JSON display
     * `return OBJECT_MAPPER_MAP.get(this).writerWithDefaultPrettyPrinter().writeValueAsString(data);`
    </pre> *
     *
     * @param data [T]
     * @return [String]
     */
    fun <T> serialize(data: T?): String?

    fun <T> prettySerialize(data: T?): String?

    fun <T> bytes(data: T?): ByteArray?

    fun <T> deserialize(data: String?, clazz: Class<T>): T?

    fun <T> deserialize(data: String?, type: Type): T?

    fun <T> deserialize(data: String?, typeReference: TypeReference<T>): T?

    fun deserialize(data: String?): JsonNode?


    @Suppress("unused")
    enum class JacksonStrategy {
        NORMAL,
        UNDER {
            override fun initialize(mapper: ObjectMapper) {
                mapper.propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
                super.initialize(mapper)
            }
        },
        IGNORE_TRANSIENT {
            override fun initialize(mapper: ObjectMapper) {
                mapper.setVisibility(
                    mapper.serializationConfig.defaultVisibilityChecker
                        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                )
                super.initialize(mapper)
            }
        },
        IGNORE_UNKNOWN {
            override fun initialize(mapper: ObjectMapper) {
                mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                super.initialize(mapper)
            }
        },
        IGNORE_NULL {
            override fun initialize(mapper: ObjectMapper) {
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
                super.initialize(mapper)
            }
        },
        KEY_NULL {
            override fun initialize(mapper: ObjectMapper) {
                mapper.serializerProvider.setNullKeySerializer(object : JsonSerializer<Any?>() {
                    @Throws(IOException::class)
                    override fun serialize(value: Any?, gen: JsonGenerator, serializers: SerializerProvider?) {
                        gen.writeFieldName("null")
                    }
                })
                super.initialize(mapper)
            }
        },
        IGNORE_TRANSIENT_AND_NULL {
            override fun initialize(mapper: ObjectMapper) {
                IGNORE_TRANSIENT.initialize(mapper)
                IGNORE_NULL.initialize(mapper)
                super.initialize(mapper)
            }
        },

        ;

        open fun initialize(mapper: ObjectMapper) {
            val javaTimeModule = JavaTimeModule()
            javaTimeModule.addSerializer(
                LocalDateTime::class.java,
                LocalDateTimeSerializer(DateTimeFormatterC.YYYY_MM_DD_HH_MM_SS)
            )
                .addSerializer(LocalDate::class.java, LocalDateSerializer(DateTimeFormatterC.YYYY_MM_DD))
                .addSerializer(YearMonth::class.java, YearMonthSerializer(DateTimeFormatterC.YYYY_MM))
                .addSerializer(LocalTime::class.java, LocalTimeSerializer(DateTimeFormatterC.HH_MM_SS))
                .addDeserializer(
                    LocalDateTime::class.java,
                    LocalDateTimeDeserializer(DateTimeFormatterC.YYYY_MM_DD_HH_MM_SS)
                )
                .addDeserializer(LocalDate::class.java, LocalDateDeserializer(DateTimeFormatterC.YYYY_MM_DD))
                .addDeserializer(YearMonth::class.java, YearMonthDeserializer(DateTimeFormatterC.YYYY_MM))
                .addDeserializer(LocalTime::class.java, LocalTimeDeserializer(DateTimeFormatterC.HH_MM_SS))
            mapper.registerModule(javaTimeModule)
        }
    }
}