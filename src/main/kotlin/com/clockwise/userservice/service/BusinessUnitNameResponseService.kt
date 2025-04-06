package com.clockwise.userservice.service

import com.clockwise.userservice.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

data class BusinessUnitNameResponse(
    val userId: String,
    val businessUnitId: String,
    val businessUnitName: String
)

@Service
class BusinessUnitNameResponseService(
    private val userRepository: UserRepository,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(BusinessUnitNameResponseService::class.java)

    suspend fun handleBusinessUnitNameResponse(message: String) {
        try {
            logger.info("Received business unit name response message: {}", message)
            val response = objectMapper.readValue(message, BusinessUnitNameResponse::class.java)
            
            val user = userRepository.findById(UUID.fromString(response.userId))
                ?: throw NoSuchElementException("User not found with ID: ${response.userId}")
            
            val updatedUser = user.copy(
                businessUnitName = response.businessUnitName
            )
            
            userRepository.save(updatedUser)
            logger.info("Successfully updated user {} with business unit name: {}", response.userId, response.businessUnitName)
        } catch (e: Exception) {
            logger.error("Error processing business unit name response", e)
            throw e
        }
    }
} 