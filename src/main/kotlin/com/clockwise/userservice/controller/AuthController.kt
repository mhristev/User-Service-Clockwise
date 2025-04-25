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
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
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
        try {
            // Validate that privacy policy has been accepted
            if (!request.privacyPolicyAccepted) {
                throw IllegalArgumentException("You must accept the privacy policy to register")
            }
            
            // Log registration attempt
            println("Registration attempt for email: ${request.email}")
            
            // Convert the registration request to a create user request with EMPLOYEE role
            val createUserRequest = CreateUserRequest(
                email = request.email,
                password = request.password,
                firstName = request.firstName,
                lastName = request.lastName,
                phoneNumber = request.phoneNumber,
                role = UserRole.EMPLOYEE,
                // Add privacy consent information
                privacyConsent = PrivacyConsent(
                    marketingConsent = request.marketingConsent,
                    analyticsConsent = request.analyticsConsent,
                    thirdPartyDataSharingConsent = request.thirdPartyDataSharingConsent
                )
            )

            println("Attempting to create user with email: ${request.email}")
            val createdUser = userService.createUser(createUserRequest)
            println("User created successfully with ID: ${createdUser.id}")
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser)
        } catch (e: Exception) {
            println("Error during registration: ${e.message}")
            e.printStackTrace()
            throw e
        }
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