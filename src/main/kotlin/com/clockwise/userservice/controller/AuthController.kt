package com.clockwise.userservice.controller

import com.clockwise.userservice.domain.PrivacyConsent
import com.clockwise.userservice.domain.UserRole
import com.clockwise.userservice.dto.ConsentUpdateRequest
import com.clockwise.userservice.service.*
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.UUID
import org.springframework.http.*

/**
 * Registration request with GDPR consent fields
 */
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val restaurantId: String? = null,
    // GDPR consent fields
    val marketingConsent: Boolean = false,
    val analyticsConsent: Boolean = false,
    val thirdPartyDataSharingConsent: Boolean = false,
    // Explicit consent acknowledgment
    val privacyPolicyAccepted: Boolean = false
)

@RestController
@RequestMapping("/v1/auth")
class AuthController(private val authService: AuthService, private val userService: UserService) {
    @PostMapping("/register")
    suspend fun register(@RequestBody request: RegisterRequest): ResponseEntity<UserDto> {
        // Validate that privacy policy has been accepted
        if (!request.privacyPolicyAccepted) {
            throw IllegalArgumentException("You must accept the privacy policy to register")
        }
        
        val currentTime = System.currentTimeMillis()
        // Set retention date 5 years from registration (lastSeenAt will be updated by the filter on subsequent requests)
        val retentionDate = currentTime + (5L * 365 * 24 * 60 * 60 * 1000)
        
        // Convert the registration request to a create user request with EMPLOYEE role
        val createUserRequest = CreateUserRequest(
            username = request.username,
            email = request.email,
            password = request.password,
            role = UserRole.EMPLOYEE,
            // Add privacy consent information
            privacyConsent = PrivacyConsent(
                marketingConsent = request.marketingConsent,
                analyticsConsent = request.analyticsConsent,
                thirdPartyDataSharingConsent = request.thirdPartyDataSharingConsent
            ),
            createdAt = currentTime,
            lastSeenAt = currentTime,
            dataRetentionDate = retentionDate
        )

        val createdUser = userService.createUser(createUserRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser)
    }

    @PostMapping("/login")
    suspend fun login(@RequestBody request: AuthRequest): Mono<ResponseEntity<AuthResponse>> {
        return authService.login(request)
            .map { ResponseEntity.ok(it) }
    }

    @PostMapping("/refresh")
    suspend fun refreshToken(@RequestBody request: RefreshTokenRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.refreshToken(request))
    }

    @PostMapping("/logout")
    suspend fun logout(): ResponseEntity<Void> {
        val username = ReactiveSecurityContextHolder.getContext()
            .map { context: SecurityContext -> context.authentication.name }
            .block() ?: throw IllegalStateException("No authenticated user found")

        val user = userService.getUserByUsername(username)
        authService.logout(user.id!!)

        return ResponseEntity.ok().build()
    }
}