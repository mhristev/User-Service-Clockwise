package com.clockwise.userservice.config

import org.mockito.Mockito
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@TestConfiguration
@EnableAutoConfiguration(exclude = [KafkaAutoConfiguration::class])
class TestKafkaConfig {

    @Bean
    @Primary
    fun kafkaListenerEndpointRegistry(): KafkaListenerEndpointRegistry {
        return KafkaListenerEndpointRegistry()
    }

    @Bean
    @Primary
    fun producerFactory(): ProducerFactory<String, String> {
        return Mockito.mock(ProducerFactory::class.java) as ProducerFactory<String, String>
    }

    @Bean
    @Primary
    fun kafkaTemplate(): KafkaTemplate<String, String> {
        val mockTemplate = Mockito.mock(KafkaTemplate::class.java) as KafkaTemplate<String, String>
        return mockTemplate
    }
} 