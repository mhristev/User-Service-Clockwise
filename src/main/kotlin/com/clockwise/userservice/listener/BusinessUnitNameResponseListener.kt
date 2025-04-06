package com.clockwise.userservice.listener

import com.clockwise.userservice.service.BusinessUnitNameResponseService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class BusinessUnitNameResponseListener(
    private val businessUnitNameResponseService: BusinessUnitNameResponseService
) {
    private val logger = LoggerFactory.getLogger(BusinessUnitNameResponseListener::class.java)

    @KafkaListener(
        topics = ["\${kafka.topic.business-unit-name-responses}"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "stringKafkaListenerContainerFactory"
    )
    suspend fun handleBusinessUnitNameResponse(message: String, ack: Acknowledgment) {
        try {
            logger.info("Received business unit name response message: {}", message)
            businessUnitNameResponseService.handleBusinessUnitNameResponse(message)
            ack.acknowledge()
            logger.info("Successfully processed business unit name response")
        } catch (e: Exception) {
            logger.error("Error processing business unit name response message", e)
            throw e
        }
    }
} 