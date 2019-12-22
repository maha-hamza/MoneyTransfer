package config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson

val jackson: ContentNegotiation.Configuration.() -> Unit = {
    jackson {
        configureJackson(this)
    }
}

val configureJackson: ObjectMapper.() -> ObjectMapper = {
    findAndRegisterModules()

    registerModule(JavaTimeModule())

    enable(SerializationFeature.INDENT_OUTPUT)
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
}

fun objectMapper(): ObjectMapper = configureJackson(jacksonObjectMapper())