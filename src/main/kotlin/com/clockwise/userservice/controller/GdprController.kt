package com.clockwise.userservice.controller

import com.clockwise.userservice.service.UserDto
import com.clockwise.userservice.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Controller for handling GDPR-related user rights requests
 */
@RestController
@RequestMapping("/v1/gdpr")
class GdprController(private val userService: UserService) {

    /**
     * Implements "Right to Erasure" (Right to be Forgotten)
     * Anonymizes a user's personal data while keeping their ID in the database
     */
    @DeleteMapping("/erase-me")
    suspend fun erasePersonalData(): ResponseEntity<Map<String, Any>> {
        // Get current user email from authentication context
        val email = ReactiveSecurityContextHolder.getContext()
            .map { it.authentication.name }
            .block() ?: throw IllegalStateException("No authenticated user found")
        
        // Get user details
        val user = userService.getUserByEmail(email)
        
        // Perform anonymization
        userService.anonymizeUser(user.id!!)
        
        // Return confirmation without sensitive info
        return ResponseEntity.ok(mapOf(
            "status" to "success",
            "message" to "Your personal data has been erased in accordance with GDPR requirements",
            "timestamp" to DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(Instant.now())
        ))
    }
    
    /**
     * Admin function to erase a specific user's data
     * This could be used when handling formal GDPR requests received via email or other channels
     */
    @DeleteMapping("/users/{userId}/erase")
    suspend fun eraseUserData(
        @PathVariable userId: String
    ): ResponseEntity<Map<String, Any>> {
        // Anonymize user data
        userService.anonymizeUser(userId)
        
        // Return confirmation
        return ResponseEntity.ok(mapOf(
            "status" to "success",
            "userId" to userId,
            "message" to "User's personal data has been erased in accordance with GDPR requirements",
            "timestamp" to DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(Instant.now())
        ))
    }
    
    /**
     * Implements "Right of Access"
     * Returns all personal data associated with the authenticated user
     */
    @GetMapping("/my-data")
    suspend fun getMyPersonalData(): ResponseEntity<UserDto> {
        // Get current user email from authentication context
        val email = ReactiveSecurityContextHolder.getContext()
            .map { it.authentication.name }
            .block() ?: throw IllegalStateException("No authenticated user found")
        
        // Get and return user details
        val user = userService.getUserByEmail(email)
        return ResponseEntity.ok(user)
    }
} 