package com.clockwise.userservice.config

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.context.annotation.Configuration

@Configuration
@EnableAutoConfiguration(exclude = [KafkaAutoConfiguration::class])
@ConditionalOnProperty(value = ["spring.profiles.active"], havingValue = "test")
class DisableKafkaAutoConfig 