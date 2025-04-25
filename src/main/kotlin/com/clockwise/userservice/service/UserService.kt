package com.clockwise.userservice.service

import com.clockwise.userservice.domain.PrivacyConsent
import com.clockwise.userservice.domain.User
import com.clockwise.userservice.domain.UserRole
import com.clockwise.userservice.dto.ConsentResponse
import com.clockwise.userservice.dto.ConsentUpdateRequest
import com.clockwise.userservice.dto.toResponse
import com.clockwise.userservice.dto.toDomain
import com.clockwise.userservice.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono
import java.util.*
import kotlin.NoSuchElementException

data class UserDto(
    val id: String?,
    val username: String,
    val email: String,
    val role: UserRole,
    val businessUnitId: String?,
    val businessUnitName: String?,
    val hasProvidedConsent: Boolean = false,
    val consentVersion: String? = null
)

data class CreateUserRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: UserRole,
    val privacyConsent: PrivacyConsent? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSeenAt: Long = System.currentTimeMillis(),
    val dataRetentionDate: Long? = null
)

data class UpdateUserRequest(
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
    val role: UserRole? = null,
    val businessUnitId: String? = null,
    val businessUnitName: String? = null
)

data class UpdateBusinessUnitRequest(
    val businessUnitId: String
)

private fun User.toDto() = UserDto(
    id = id,
    username = username,
    email = email,
    role = role,
    businessUnitId = businessUnitId,
    businessUnitName = businessUnitName,
    hasProvidedConsent = privacyConsent != null,
    consentVersion = consentVersion
)

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val transactionalOperator: TransactionalOperator,
    private val kafkaProducerService: KafkaProducerService
): ReactiveUserDetailsService {
    private val logger = LoggerFactory.getLogger(UserService::class.java)

    suspend fun createUser(request: CreateUserRequest): UserDto {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already in use")
        }

        if (userRepository.existsByUsername(request.username)) {
            throw IllegalArgumentException("Username already in use")
        }

        // Determine current privacy policy version - in a real system, this would be fetched from a config
        val currentConsentVersion = "1.0"

        val user = User(
            username = request.username,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            role = request.role,
            businessUnitId = null,
            businessUnitName = null,
            privacyConsent = request.privacyConsent,
            consentVersion = if (request.privacyConsent != null) currentConsentVersion else null,
            createdAt = request.createdAt,
            lastSeenAt = request.lastSeenAt,
            dataRetentionDate = request.dataRetentionDate
        )
        
        val savedUser = userRepository.save(user)
        val userDto = savedUser.toDto()

        // Log consent for audit trail if consent was provided
        if (request.privacyConsent != null) {
            logger.info("User created with initial privacy consent: ${request.privacyConsent}")
        }

        return userDto
    }


    suspend fun getUserById(id: String): UserDto {
        val userDto = userRepository.findById(id)?.toDto()
            ?: throw NoSuchElementException("User not found with ID: $id")
//        logger.info("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC")
//        val businessUnitName = userDto.restaurantId?.let { businessUnitCacheService.getBusinessUnitName(it) }
//        logger.info("NBBBBBBBNBBBBBBBNBBBBBBBNBBBBBBBNBBBBBBB $businessUnitName")
//        userDto.businessUnitName = businessUnitName
        return userDto

    }

    suspend fun getUserByUsername(username: String): UserDto {
        return userRepository.findByUsername(username)?.toDto()
            ?: throw NoSuchElementException("User not found with username: $username")
    }

    suspend fun updateUser(id: String, request: UpdateUserRequest): UserDto {
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
            businessUnitId = request.businessUnitId ?: user.businessUnitId,
            businessUnitName = request.businessUnitName ?: user.businessUnitName
        )

        return userRepository.save(updatedUser).toDto()
    }

    suspend fun deleteUser(id: UUID) {
        if (!userRepository.existsById(id)) {
            throw NoSuchElementException("User not found with ID: $id")
        }
        userRepository.deleteById(id)
    }

    fun getUsersByRestaurantId(restaurantId: String): Flow<UserDto> {
        return userRepository.findAllByBusinessUnitId(restaurantId).map { it.toDto() }
    }

    fun getUsersWithoutBusinessUnit(email: String? = null): Flow<UserDto> {
        logger.info("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
        val u = userRepository.findAllByBusinessUnitIdIsNullAndEmail(email)
        logger.info("asdaaaaasdaaaaasdaaaaasdaaaaasdaaaaasdaaaaasdaaaa")
        return userRepository.findAllByBusinessUnitIdIsNullAndEmail(email)
            .map { it.toDto() }
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

    suspend fun updateUserBusinessUnit(id: String, request: UpdateBusinessUnitRequest): UserDto {
        val user = userRepository.findById(id)
            ?: throw NoSuchElementException("User not found with ID: $id")

        val updatedUser = user.copy(
            businessUnitId = request.businessUnitId
        )

        val savedUser = userRepository.save(updatedUser).toDto()
        
        // Send Kafka message to request business unit name
        kafkaProducerService.requestBusinessUnitName(id, request.businessUnitId)
        
        return savedUser
    }

    /**
     * Update a user's privacy consent settings
     * 
     * @param userId The ID of the user
     * @param request The consent update request
     * @return The updated consent information
     */
    suspend fun updateUserConsent(userId: String, request: ConsentUpdateRequest): ConsentResponse {
        val user = userRepository.findById(userId)
            ?: throw NoSuchElementException("User not found with ID: $userId")
        
        // Determine current privacy policy version - in a real system, this would be fetched from a config
        val currentConsentVersion = "1.0"
            
        val updatedConsent = request.toDomain(user.privacyConsent)
        
        val updatedUser = user.copy(
            privacyConsent = updatedConsent,
            consentVersion = currentConsentVersion
        )
        
        val savedUser = userRepository.save(updatedUser)
        
        // Log consent for audit trail
        logger.info("Updated privacy consent for user $userId: $updatedConsent")
        
        return savedUser.privacyConsent!!.toResponse(userId, savedUser.consentVersion)
    }
    
    /**
     * Get a user's current privacy consent settings
     * 
     * @param userId The ID of the user
     * @return The user's consent information
     */
    suspend fun getUserConsent(userId: String): ConsentResponse {
        val user = userRepository.findById(userId)
            ?: throw NoSuchElementException("User not found with ID: $userId")
            
        val privacyConsent = user.privacyConsent ?: PrivacyConsent()
        
        return privacyConsent.toResponse(userId, user.consentVersion)
    }
    
    /**
     * Withdraw all user consent
     * 
     * @param userId The ID of the user
     * @return The updated consent information with all consent flags set to false
     */
    suspend fun withdrawAllConsent(userId: String): ConsentResponse {
        val user = userRepository.findById(userId)
            ?: throw NoSuchElementException("User not found with ID: $userId")
            
        val withdrawnConsent = PrivacyConsent(
            marketingConsent = false,
            analyticsConsent = false,
            thirdPartyDataSharingConsent = false,
            consentTimestamp = user.privacyConsent?.consentTimestamp ?: System.currentTimeMillis()
        )
        
        val updatedUser = user.copy(
            privacyConsent = withdrawnConsent
        )
        
        val savedUser = userRepository.save(updatedUser)
        
        // Log consent withdrawal for audit trail
        logger.info("All consent withdrawn for user $userId")
        
        return savedUser.privacyConsent!!.toResponse(userId, savedUser.consentVersion)
    }
    
    /**
     * Set the data retention date for a user
     * 
     * @param userId The ID of the user
     * @param retentionPeriodDays Number of days before data should be deleted/anonymized
     * @return The updated user DTO
     */
    suspend fun setDataRetentionDate(userId: String, retentionPeriodDays: Int): UserDto {
        val user = userRepository.findById(userId)
            ?: throw NoSuchElementException("User not found with ID: $userId")
            
        // Calculate retention date based on current time plus retention period
        val retentionDate = System.currentTimeMillis() + (retentionPeriodDays * 24 * 60 * 60 * 1000L)
        
        val updatedUser = user.copy(
            dataRetentionDate = retentionDate
        )
        
        val savedUser = userRepository.save(updatedUser)
        
        logger.info("Set data retention date for user $userId to ${Date(retentionDate)}")
        
        return savedUser.toDto()
    }
}