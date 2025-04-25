package com.clockwise.userservice.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

enum class UserRole {
    ADMIN, MANAGER, EMPLOYEE
}

/**
 * Represents the user's privacy consent choices for different data processing purposes
 * as required by GDPR Article 7.
 */
data class PrivacyConsent(
    val marketingConsent: Boolean = false,
    val analyticsConsent: Boolean = false,
    val thirdPartyDataSharingConsent: Boolean = false,
    val consentTimestamp: Long = System.currentTimeMillis()
)

@Table("users")
data class User(
    @Id
    val id: String? = null,
    val username: String,
    val email: String,
    val password: String,
    val role: UserRole,
    val businessUnitId: String? = null,
    val businessUnitName: String? = null,
    
    // GDPR-related fields
    val privacyConsent: PrivacyConsent? = null,
    val dataRetentionDate: Long? = null,    // Date when user data should be deleted/anonymized
    val consentVersion: String? = null,     // Tracks which version of privacy policy user agreed to
    
    // User activity tracking for GDPR retention purposes
    val createdAt: Long = System.currentTimeMillis(),
    val lastSeenAt: Long = System.currentTimeMillis()
)