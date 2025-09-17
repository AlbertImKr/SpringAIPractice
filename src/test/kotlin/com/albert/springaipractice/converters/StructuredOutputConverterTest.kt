package com.albert.springaipractice.converters

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.ai.converter.BeanOutputConverter
import org.springframework.ai.converter.ListOutputConverter
import org.springframework.ai.converter.MapOutputConverter

class StructuredOutputConverterTest {

    @Test
    fun beanOutputConverterTest() {
        val converter = BeanOutputConverter(Person::class.java)
        val input = """
            {
              "name": "John Doe",
              "age": 30
            }
        """.trimIndent()

        val person = converter.convert(input) ?: throw IllegalArgumentException("Conversion failed")

        assertThat(person.name).isEqualTo("John Doe")
        assertThat(person.age).isEqualTo(30)

        val format = converter.getFormat()

        println(format)

        val schemaMap = converter.jsonSchemaMap

        println(schemaMap)
    }

    @Test
    fun mapOutputConverterTest() {
        val converter = MapOutputConverter()

        val input = """
            {
              "name": "Jane Doe",
              "age": 25
            }
        """.trimIndent()

        val resultMap = converter.convert(input) ?: throw IllegalArgumentException("Conversion failed")

        println(resultMap)
        assertThat(resultMap["name"]).isEqualTo("Jane Doe")
        assertThat(resultMap["age"]).isEqualTo(25)

        val format = converter.getFormat()
        println(format)
    }

    @Test
    fun listOutputConverterTest() {
        val converter = ListOutputConverter()

        val input = """
            Allice, Albert, Bob, Carol
        """.trimIndent()

        val resultList = converter.convert(input) ?: throw IllegalArgumentException("Conversion failed")

        println(resultList)
        assertThat(resultList).hasSize(4)

        val format = converter.format
        println(format)
    }

    class Person(val name: String, val age: Int)
}
