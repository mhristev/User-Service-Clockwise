package com.clockwise.userservice.service

import com.clockwise.userservice.repository.UserRepository
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import java.util.*

/**
 * Service to handle GDPR data retention requirements
 * Users will be anonymized or deleted based on:
 * Inactivity period (lastSeenAt older than configured threshold)
 */
@Service
class DataRetentionService(
    private val userRepository: UserRepository,
    private val transactionalOperator: TransactionalOperator,
    private val userService: UserService
) {
    private val logger = LoggerFactory.getLogger(DataRetentionService::class.java)
    
    // Default retention period for inactive users: 10 years
    private val DEFAULT_INACTIVITY_RETENTION_PERIOD_MS = 10L * 365 * 24 * 60 * 60 * 1000
    
    /**
     * Check for users that need to be anonymized or deleted daily at 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    fun processDataRetention() {
        logger.info("Starting scheduled data retention check")
        runBlocking {
            processDataRetentionInternal()
        }
        logger.info("Completed scheduled data retention check")
    }
    
    private suspend fun processDataRetentionInternal() {
//        val currentTime = System.currentTimeMillis()
//
//        // Find users inactive for longer than the retention period
//        val inactiveRetentionThreshold = currentTime - DEFAULT_INACTIVITY_RETENTION_PERIOD_MS
//        val inactiveUsers = userRepository.findAll().filter { user ->
//            user.lastSeenAt <= inactiveRetentionThreshold
//        }
//
//        logger.info("Found ${inactiveUsers.size} inactive users to process for data retention")
//
//        for (user in inactiveUsers) {
//            try {
//                // Use the anonymizeUser method for consistent handling
//                user.id?.let { userId ->
//                    userService.anonymizeUser(userId)
//                    logger.info("Anonymized inactive user with ID: $userId based on retention policy")
//                }
//            } catch (e: Exception) {
//                logger.error("Error processing data retention for user ID: ${user.id}", e)
//            }
//        }
    }
} 