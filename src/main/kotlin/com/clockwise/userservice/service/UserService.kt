package com.clockwise.userservice.service

import com.clockwise.userservice.domain.User
import com.clockwise.userservice.domain.UserRole
import com.clockwise.userservice.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.*
import kotlin.NoSuchElementException

data class UserDto(
    val id: String?,
    val username: String,
    val email: String,
    val role: UserRole,
    val restaurantId: String?
)

data class CreateUserRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: UserRole,
    val restaurantId: String?
)

data class UpdateUserRequest(
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
    val role: UserRole? = null,
    val restaurantId: String? = null
)

private fun User.toDto() = UserDto(
    id = id,
    username = username,
    email = email,
    role = role,
    restaurantId = restaurantId
)

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
): ReactiveUserDetailsService {

    suspend fun createUser(request: CreateUserRequest): UserDto {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already in use")
        }

        if (userRepository.existsByUsername(request.username)) {
            throw IllegalArgumentException("Username already in use")
        }

        val user = User(
            username = request.username,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            role = request.role,
            restaurantId = request.restaurantId
        )

        return userRepository.save(user).toDto()
    }

    suspend fun getUserById(id: String): UserDto {
        return userRepository.findById(id)?.toDto()
            ?: throw NoSuchElementException("User not found with ID: $id")
    }

    suspend fun getUserByUsername(username: String): UserDto {
        return userRepository.findByUsername(username)?.toDto()
            ?: throw NoSuchElementException("User not found with username: $username")
    }

    suspend fun updateUser(id: UUID, request: UpdateUserRequest): UserDto {
        val user = userRepository.findById(id)
            ?: throw NoSuchElementException("User not found with ID: $id")

        // Check if email is being updated and is already in use by another user
        if (request.email != null && request.email != user.email && userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already in use")
        }

        // Check if username is being updated and is already in use by another user
        if (request.username != null && request.username != user.username && userRepository.existsByUsername(request.username)) {
            throw IllegalArgumentException("Username already in use")
        }

        val updatedUser = user.copy(
            username = request.username ?: user.username,
            email = request.email ?: user.email,
            password = request.password?.let { passwordEncoder.encode(it) } ?: user.password,
            role = request.role ?: user.role,
            restaurantId = request.restaurantId ?: user.restaurantId
        )

        return userRepository.save(updatedUser).toDto()
    }

    suspend fun deleteUser(id: UUID) {
        if (!userRepository.existsById(id)) {
            throw NoSuchElementException("User not found with ID: $id")
        }
        userRepository.deleteById(id)
    }

    fun getUsersByRestaurantId(restaurantId: UUID): Flow<UserDto> {
        return userRepository.findAllByRestaurantId(restaurantId).map { it.toDto() }
    }

    override fun findByUsername(username: String): Mono<UserDetails> {
        return Mono.fromSupplier {
            kotlinx.coroutines.runBlocking {
                userRepository.findByUsername(username)?.let { user ->
                    org.springframework.security.core.userdetails.User.builder()
                        .username(user.username)
                        .password(user.password)
                        .authorities(SimpleGrantedAuthority(user.role.name))
                        .build()
                }
            }
        }
    }
}