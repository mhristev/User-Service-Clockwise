package com.clockwise.userservice.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class BusinessUnitCacheService {
    private val logger = LoggerFactory.getLogger(BusinessUnitCacheService::class.java)
    private val cache = ConcurrentHashMap<String, String>()

    fun cacheBusinessUnitName(id: String, name: String) {
        try {
            logger.info("Caching business unit name: id={}, name={}", id, name)
            cache[id] = name
            logger.info("Successfully cached business unit name")
        } catch (e: Exception) {
            logger.error("Error caching business unit name: id={}, name={}", id, name, e)
            throw e
        }
    }

    fun getBusinessUnitName(id: String): String? {
        try {
            logger.info("Getting business unit name for id: {}", id)
            val name = cache[id]
            logger.info("Retrieved business unit name: {}", name)
            return name
        } catch (e: Exception) {
            logger.error("Error getting business unit name for id: {}", id, e)
            throw e
        }
    }

    fun removeBusinessUnitName(id: String) {
        try {
            logger.info("Removing business unit name for id: {}", id)
            cache.remove(id)
            logger.info("Successfully removed business unit name")
        } catch (e: Exception) {
            logger.error("Error removing business unit name for id: {}", id, e)
            throw e
        }
    }
} 