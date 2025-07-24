package com.github.morningzeng.toolbox.enums

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.dataformat.csv.CsvFactory
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsFactory
import com.fasterxml.jackson.dataformat.toml.TomlFactory
import com.fasterxml.jackson.dataformat.xml.XmlFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.github.morningzeng.toolbox.utils.JacksonSupport
import com.github.morningzeng.toolbox.utils.JacksonSupport.JacksonStrategy
import java.lang.reflect.Type

/**
 * @author Morning Zeng
 * @since 2025-05-16
 */
enum class JacksonType(
    val suffix: String
) : JacksonSupport {

    CSV("csv"),
    JSON("json"),
    PROPERTIES("properties"),
    TOML("toml"),
    XML("xml"),
    YAML("yaml")

    ;

    companion object {
        private val objectMapperMap: Map<JacksonType, Lazy<ObjectMapper>> = JacksonType.entries.associateWith { type ->
            lazy {
                val factory = when (type) {
                    CSV -> CsvFactory()
                    JSON -> JsonFactory()
                    PROPERTIES -> JavaPropsFactory()
                    TOML -> TomlFactory()
                    XML -> XmlFactory()
                    YAML -> YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                }
                ObjectMapper(factory).apply {
                    JacksonStrategy.IGNORE_TRANSIENT_AND_NULL.initialize(this)
                    propertyNamingStrategy = PropertyNamingStrategies.KEBAB_CASE
                }
            }
        }

    }

    fun mapper(): ObjectMapper {
        return objectMapperMap.getValue(this).value
    }

    fun fullName(filename: String): String = "${filename}.${suffix}"

    override fun <T> serialize(data: T?): String? = JacksonSupport.withMapper(this.mapper()).serialize(data)

    override fun <T> prettySerialize(data: T?): String? = JacksonSupport.withMapper(this.mapper()).prettySerialize(data)

    override fun <T> bytes(data: T?): ByteArray? = JacksonSupport.withMapper(this.mapper()).bytes(data)

    override fun <T> deserialize(data: String?, clazz: Class<T>): T? =
        JacksonSupport.withMapper(this.mapper()).deserialize(data, clazz)

    override fun <T> deserialize(data: String?, type: Type): T? =
        JacksonSupport.withMapper(this.mapper()).deserialize(data, type)

    override fun <T> deserialize(
        data: String?,
        typeReference: TypeReference<T>
    ): T? = JacksonSupport.withMapper(this.mapper()).deserialize(data, typeReference)

    override fun deserialize(data: String?): JsonNode? = JacksonSupport.withMapper(this.mapper()).deserialize(data)
}