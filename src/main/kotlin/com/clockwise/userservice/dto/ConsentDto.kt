package com.clockwise.userservice.dto

import com.clockwise.userservice.domain.PrivacyConsent

/**
 * Data transfer object for updating user consent preferences
 */
data class ConsentUpdateRequest(
    val marketingConsent: Boolean,
    val analyticsConsent: Boolean,
    val thirdPartyDataSharingConsent: Boolean
)

/**
 * Response object for consent operations
 */
data class ConsentResponse(
    val userId: String,
    val marketingConsent: Boolean,
    val analyticsConsent: Boolean,
    val thirdPartyDataSharingConsent: Boolean,
    val consentTimestamp: Long,
    val consentVersion: String?
)

/**
 * Extension function to convert domain PrivacyConsent to ConsentResponse
 */
fun PrivacyConsent.toResponse(userId: String, consentVersion: String?) = ConsentResponse(
    userId = userId,
    marketingConsent = marketingConsent,
    analyticsConsent = analyticsConsent,
    thirdPartyDataSharingConsent = thirdPartyDataSharingConsent,
    consentTimestamp = consentTimestamp,
    consentVersion = consentVersion
)

/**
 * Extension function to convert ConsentUpdateRequest to domain PrivacyConsent
 */
fun ConsentUpdateRequest.toDomain(previousConsent: PrivacyConsent? = null) = PrivacyConsent(
    marketingConsent = marketingConsent,
    analyticsConsent = analyticsConsent,
    thirdPartyDataSharingConsent = thirdPartyDataSharingConsent,
    consentTimestamp = previousConsent?.consentTimestamp ?: System.currentTimeMillis()
) 