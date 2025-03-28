package com.clockwise.userservice.service

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class BusinessUnitCacheService(
    private val redisTemplate: RedisTemplate<String, String>
) {
    private val keyPrefix = "business-unit:"

    fun cacheBusinessUnitName(id: String, name: String) {
        redisTemplate.opsForValue().set("$keyPrefix$id", name)
    }

    fun getBusinessUnitName(id: String): String? {
        return redisTemplate.opsForValue().get("$keyPrefix$id")
    }

    fun removeBusinessUnitName(id: String) {
        redisTemplate.delete("$keyPrefix$id")
    }
} 