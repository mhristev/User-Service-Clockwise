package com.clockwise.userservice.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

data class BusinessUnitNameRequest(
    val userId: String,
    val businessUnitId: String
)

data class UserActivityEvent(
    val userId: String,
    val timestamp: Long,
    val source: String = "user-service"
)

@Service
class KafkaProducerService(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(KafkaProducerService::class.java)

    @Value("\${kafka.topic.business-unit-name-requests}")
    private lateinit var businessUnitNameRequestsTopic: String
    
    @Value("\${kafka.topic.user-activity}")
    private lateinit var userActivityTopic: String

    fun requestBusinessUnitName(userId: String, businessUnitId: String) {
        try {
            val request = BusinessUnitNameRequest(userId, businessUnitId)
            val message = objectMapper.writeValueAsString(request)
            logger.info("Sending business unit name request: {}", message)
            kafkaTemplate.send(businessUnitNameRequestsTopic, message)

        } catch (e: Exception) {
            logger.error("Error preparing business unit name request", e)
            throw e
        }
    }
} 