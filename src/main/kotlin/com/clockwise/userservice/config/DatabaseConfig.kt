package com.clockwise.userservice.config

import com.clockwise.userservice.domain.PrivacyConsent
import com.fasterxml.jackson.databind.ObjectMapper
import io.r2dbc.postgresql.codec.Json
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.PostgresDialect
import java.util.ArrayList

@Configuration
class DatabaseConfig {

    @Bean
    fun r2dbcCustomConversions(objectMapper: ObjectMapper): R2dbcCustomConversions {
        val converters = ArrayList<Any>()
        
        // Add converters for PrivacyConsent
        converters.add(PrivacyConsentToJsonConverter(objectMapper))
        converters.add(JsonToPrivacyConsentConverter(objectMapper))
        
        return R2dbcCustomConversions.of(PostgresDialect.INSTANCE, converters)
    }

    @WritingConverter
    class PrivacyConsentToJsonConverter(private val objectMapper: ObjectMapper) : Converter<PrivacyConsent, Json> {
        override fun convert(source: PrivacyConsent): Json {
            return Json.of(objectMapper.writeValueAsString(source))
        }
    }

    @ReadingConverter
    class JsonToPrivacyConsentConverter(private val objectMapper: ObjectMapper) : Converter<Json, PrivacyConsent> {
        override fun convert(source: Json): PrivacyConsent {
            return objectMapper.readValue(source.asString(), PrivacyConsent::class.java)
                ?: PrivacyConsent() // Return default object if null
        }
    }
} 