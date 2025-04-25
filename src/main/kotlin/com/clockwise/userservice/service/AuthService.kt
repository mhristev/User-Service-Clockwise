package com.clockwise.userservice.service

import com.clockwise.userservice.config.JwtUtils
import com.clockwise.userservice.domain.RefreshToken
import com.clockwise.userservice.repository.RefreshTokenRepository
import kotlinx.coroutines.reactor.mono
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.*

data class AuthRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: UserDto
)

data class RefreshTokenRequest(
    val refreshToken: String
)

@Service
class AuthService(
    private val userService: UserService,
    private val userDetailsService: ReactiveUserDetailsService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtils: JwtUtils,
    private val refreshTokenRepository: RefreshTokenRepository
) {

    fun login(request: AuthRequest): Mono<AuthResponse> {
        return userDetailsService.findByUsername(request.email)
            .switchIfEmpty(Mono.error(IllegalArgumentException("User not found with email: ${request.email}")))
            .flatMap { userDetails ->
                if (passwordEncoder.matches(request.password, userDetails.password)) {
                    mono {
                        val user = userService.getUserByEmail(userDetails.username)
                        val accessToken = jwtUtils.generateToken(userDetails)
                        val refreshToken = generateRefreshToken(user.id!!)

                        AuthResponse(
                            token = accessToken,
                            refreshToken = refreshToken,
                            expiresIn = jwtUtils.getExpirationDate(accessToken).time - System.currentTimeMillis(),
                            user = user
                        )
                    }
                } else {
                    Mono.error(IllegalArgumentException("Incorrect password"))
                }
            }
    }

    suspend fun refreshToken(request: RefreshTokenRequest): AuthResponse {
        val refreshToken = refreshTokenRepository.findByToken(request.refreshToken)
            ?: throw IllegalArgumentException("Invalid refresh token")

        if (refreshToken.expiryDate < System.currentTimeMillis() || refreshToken.isRevoked) {
            throw IllegalArgumentException("Refresh token expired or revoked")
        }

        val user = userService.getUserById(refreshToken.userId)
        val userDetails = userDetailsService.findByUsername(user.email).block()
            ?: throw IllegalStateException("User not found for refresh token")

        val accessToken = jwtUtils.generateToken(userDetails)

        return AuthResponse(
            token = accessToken,
            refreshToken = request.refreshToken,
            expiresIn = jwtUtils.getExpirationDate(accessToken).time - System.currentTimeMillis(),
            user = user
        )
    }

    suspend fun logout(userId: String) {
        refreshTokenRepository.revokeAllUserTokens(userId)
    }

    private suspend fun generateRefreshToken(userId: String): String {
        // Revoke any existing refresh tokens for this user
        refreshTokenRepository.revokeAllUserTokens(userId)

        val token = UUID.randomUUID().toString()
        val expiryDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000) // 7 days

        val refreshToken = RefreshToken(
            token = token,
            userId = userId,
            expiryDate = expiryDate
        )

        refreshTokenRepository.save(refreshToken)
        return token
    }
}