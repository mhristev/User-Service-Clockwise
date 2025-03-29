package com.clockwise.userservice.service

import com.clockwise.orgservice.EventType
import com.clockwise.orgservice.BusinessUnitEvent
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import com.fasterxml.jackson.databind.ObjectMapper

@Service
class BusinessUnitEventService(
    private val objectMapper: ObjectMapper,
    private val businessUnitCacheService: BusinessUnitCacheService
) {
    private val logger = LoggerFactory.getLogger(BusinessUnitEventService::class.java)

    @KafkaListener(
        topics = ["\${kafka.topic.business-unit-events}"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleBusinessUnitEvent(event: BusinessUnitEvent, ack: Acknowledgment) {
        try {
            logger.info("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA, ${event}")
            logger.debug("Raw event received: {}", objectMapper.writeValueAsString(event))
            logger.info("Processing BusinessUnit event: id=${event.id}, name=${event.name}, type=${event.type}")
            
            when (event.type) {
                EventType.CREATED -> handleBusinessUnitCreated(event)
                EventType.UPDATED -> handleBusinessUnitUpdated(event)
                EventType.DELETED -> handleBusinessUnitDeleted(event)
            }
            ack.acknowledge()
            logger.info("Successfully processed and acknowledged event: id=${event.id}")
        } catch (e: Exception) {
            logger.error("Error processing BusinessUnit event: $event", e)
            // Don't acknowledge the message if there's an error
            throw e
        }
    }

    private fun handleBusinessUnitCreated(event: BusinessUnitEvent) {
        logger.info("Processing BusinessUnit Created Event: id=${event.id}, name=${event.name}")

        businessUnitCacheService.cacheBusinessUnitName(event.id, event.name)
    }

    private fun handleBusinessUnitUpdated(event: BusinessUnitEvent) {
        logger.info("Processing BusinessUnit Updated Event: id=${event.id}, name=${event.name}")
        businessUnitCacheService.cacheBusinessUnitName(event.id, event.name)
    }

    private fun handleBusinessUnitDeleted(event: BusinessUnitEvent) {
        logger.info("Processing BusinessUnit Deleted Event: id=${event.id}, name=${event.name}")
        businessUnitCacheService.removeBusinessUnitName(event.id)
    }
} 