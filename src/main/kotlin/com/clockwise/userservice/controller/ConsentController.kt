package com.clockwise.userservice.controller

import com.clockwise.userservice.dto.ConsentResponse
import com.clockwise.userservice.dto.ConsentUpdateRequest
import com.clockwise.userservice.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controller for managing GDPR-related user consent
 */
@RestController
@RequestMapping("/v1/users/consent")
class ConsentController(private val userService: UserService) {

    /**
     * Get a user's current consent status
     */
    @GetMapping("/{userId}")
    suspend fun getUserConsent(@PathVariable userId: String): ResponseEntity<ConsentResponse> {
        return ResponseEntity.ok(userService.getUserConsent(userId))
    }

    /**
     * Update a user's consent preferences
     */
    @PutMapping("/{userId}")
    suspend fun updateUserConsent(
        @PathVariable userId: String,
        @RequestBody request: ConsentUpdateRequest
    ): ResponseEntity<ConsentResponse> {
        return ResponseEntity.ok(userService.updateUserConsent(userId, request))
    }

    /**
     * Withdraw all consent for a user
     */
    @DeleteMapping("/{userId}")
    suspend fun withdrawAllConsent(@PathVariable userId: String): ResponseEntity<ConsentResponse> {
        return ResponseEntity.ok(userService.withdrawAllConsent(userId))
    }
    
    /**
     * Set data retention period for a user
     */
    @PutMapping("/{userId}/retention")
    suspend fun setDataRetentionPeriod(
        @PathVariable userId: String,
        @RequestParam retentionPeriodDays: Int
    ): ResponseEntity<Map<String, Any>> {
        userService.setDataRetentionDate(userId, retentionPeriodDays)
        return ResponseEntity.ok(mapOf(
            "userId" to userId,
            "retentionPeriodDays" to retentionPeriodDays,
            "message" to "Data retention period set successfully"
        ))
    }
} 