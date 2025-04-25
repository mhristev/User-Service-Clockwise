package com.clockwise.userservice.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class PrivacyPolicyResponse(
    val version: String,
    val lastUpdated: String,
    val content: String,
    val dataProcessingPurposes: List<DataProcessingPurpose>
)

data class DataProcessingPurpose(
    val id: String,
    val name: String,
    val description: String
)

/**
 * Controller for serving privacy policy information
 */
@RestController
@RequestMapping("/v1/privacy-policy")
class PrivacyPolicyController {

    /**
     * Get the current privacy policy information
     */
    @GetMapping
    fun getPrivacyPolicy(): ResponseEntity<PrivacyPolicyResponse> {
        // In a real application, this would be retrieved from a database or configuration
        val privacyPolicy = PrivacyPolicyResponse(
            version = "1.0",
            lastUpdated = "2024-05-22",
            content = """
                # Privacy Policy
                
                ## Introduction
                
                ClockWise is committed to protecting the privacy of our users. This privacy policy explains 
                how we collect, use, and safeguard your personal information when you use our service.
                
                ## Data We Collect
                
                We collect the following personal information:
                - Basic account information (name, email, username)
                - Business unit association
                - User role and permissions
                
                ## How We Use Your Data
                
                We use your data for the following purposes:
                - To provide and maintain our service
                - To notify you about changes to our service
                - To provide customer support
                - To gather analysis or valuable information so that we can improve our service
                - To monitor the usage of our service
                
                ## Your Data Protection Rights
                
                Under GDPR, you have the following rights:
                - The right to access your personal data
                - The right to rectification
                - The right to erasure
                - The right to restrict processing
                - The right to data portability
                - The right to object
                
                ## Contact Us
                
                If you have any questions about this privacy policy, please contact us at privacy@clockwise-app.com
            """.trimIndent(),
            dataProcessingPurposes = listOf(
                DataProcessingPurpose(
                    id = "marketing",
                    name = "Marketing",
                    description = "We use your data to send you newsletters, promotions, and other marketing communications."
                ),
                DataProcessingPurpose(
                    id = "analytics",
                    name = "Analytics",
                    description = "We use analytics data to improve our service and understand how users interact with our platform."
                ),
                DataProcessingPurpose(
                    id = "third-party",
                    name = "Third-party Sharing",
                    description = "We may share your data with trusted third-party service providers who assist us in operating our service."
                )
            )
        )
        
        return ResponseEntity.ok(privacyPolicy)
    }
} 