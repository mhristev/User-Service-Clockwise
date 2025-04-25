package com.clockwise.userservice.service

import com.clockwise.userservice.domain.PrivacyConsent
import com.clockwise.userservice.domain.User
import com.clockwise.userservice.domain.UserRole
import com.clockwise.userservice.domain.UserStatus
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
    val email: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String?,
    val role: UserRole,
    val businessUnitId: String?,
    val businessUnitName: String?,
    val hasProvidedConsent: Boolean = false,
    val consentVersion: String? = null,
    val createdAt: Long? = null,
    val lastSeenAt: Long? = null,
    val userStatus: UserStatus = UserStatus.ACTIVE
)

data class CreateUserRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String? = null,
    val role: UserRole,
    val privacyConsent: PrivacyConsent? = null
)

data class UpdateUserRequest(
    val email: String? = null,
    val password: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null,
    val role: UserRole? = null,
    val businessUnitId: String? = null,
    val businessUnitName: String? = null
)

data class UpdateBusinessUnitRequest(
    val businessUnitId: String
)

private fun User.toDto() = UserDto(
    id = id,
    email = email,
    firstName = firstName,
    lastName = lastName,
    phoneNumber = phoneNumber,
    role = role,
    businessUnitId = businessUnitId,
    businessUnitName = businessUnitName,
    hasProvidedConsent = privacyConsent != null,
    consentVersion = consentVersion,
    createdAt = createdAt,
    lastSeenAt = lastSeenAt,
    userStatus = userStatus
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
        try {
            logger.info("Checking if email ${request.email} already exists")
            
            val emailExists = try {
                userRepository.existsByEmail(request.email)
            } catch (e: Exception) {
                logger.error("Error checking if email exists: ${e.message}", e)
                // Fallback to using alternative method if the first one fails
                try {
                    userRepository.findByEmail(request.email) != null
                } catch (e2: Exception) {
                    logger.error("Error using fallback email check: ${e2.message}", e2)
                    false  // Assume email doesn't exist if both checks fail
                }
            }
            
            logger.info("Email check result: $emailExists")
            
            if (emailExists) {
                logger.warn("Registration failed: Email ${request.email} already in use")
                throw IllegalArgumentException("Email already in use")
            }

            // Determine current privacy policy version - in a real system, this would be fetched from a config
            val currentConsentVersion = "1.0"

            logger.info("Creating new user with email: ${request.email}")
            val user = User(
                email = request.email,
                password = passwordEncoder.encode(request.password),
                firstName = request.firstName,
                lastName = request.lastName,
                phoneNumber = request.phoneNumber,
                role = request.role,
                businessUnitId = null,
                businessUnitName = null,
                privacyConsent = request.privacyConsent,
                consentVersion = if (request.privacyConsent != null) currentConsentVersion else null,
                createdAt = System.currentTimeMillis(),
                lastSeenAt = System.currentTimeMillis(),
                userStatus = UserStatus.ACTIVE
            )
            
            logger.info("Saving user to database")
            val savedUser = userRepository.save(user)
            val userDto = savedUser.toDto()

            // Log consent for audit trail if consent was provided
            if (request.privacyConsent != null) {
                logger.info("User created with initial privacy consent: ${request.privacyConsent}")
            }

            logger.info("User created successfully with ID: ${userDto.id}")
            return userDto
        } catch (e: Exception) {
            logger.error("Error creating user: ${e.message}", e)
            throw e
        }
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

    suspend fun getUserByEmail(email: String): UserDto {
        return userRepository.findByEmail(email)?.toDto()
            ?: throw NoSuchElementException("User not found with email: $email")
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

        val updatedUser = user.copy(
            email = request.email ?: user.email,
            password = request.password?.let { passwordEncoder.encode(it) } ?: user.password,
            firstName = request.firstName ?: user.firstName,
            lastName = request.lastName ?: user.lastName,
            phoneNumber = request.phoneNumber ?: user.phoneNumber,
            role = request.role ?: user.role,
            businessUnitId = request.businessUnitId ?: user.businessUnitId,
            businessUnitName = request.businessUnitName ?: user.businessUnitName,
            lastSeenAt = System.currentTimeMillis() // Update last seen timestamp on profile updates
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
        logger.debug("Getting users without business unit" + (email?.let { " with email filter: $it" } ?: ""))
        return userRepository.findAllByBusinessUnitIdIsNullAndEmail(email)
            .map { it.toDto() }
    }

    override fun findByUsername(username: String): Mono<UserDetails> {
        return Mono.fromSupplier {
            kotlinx.coroutines.runBlocking {
                userRepository.findByEmail(username)?.let { user ->
                    org.springframework.security.core.userdetails.User.builder()
                        .username(user.email)
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
    
    // New method to update last seen timestamp
    suspend fun updateLastSeen(userId: String): UserDto? {
        return userRepository.updateLastSeenAt(userId, System.currentTimeMillis())?.toDto()
    }

    /**
     * Anonymize a user's personal data while keeping their ID in the database (GDPR right to erasure)
     * 
     * @param userId The ID of the user to anonymize
     * @return The anonymized user DTO
     */
    suspend fun anonymizeUser(userId: String): UserDto {
        val user = userRepository.findById(userId)
            ?: throw NoSuchElementException("User not found with ID: $userId")
        
        // Generate random suffix to ensure uniqueness while maintaining anonymity
        val anonymousSuffix = UUID.randomUUID().toString().substring(0, 8)
        
        // Create anonymized user - keep ID but replace all personal information
        val anonymizedUser = user.copy(
            email = "anonymized-$anonymousSuffix@redacted.local",
            password = passwordEncoder.encode(UUID.randomUUID().toString()), // Reset password
            firstName = "[REDACTED]",
            lastName = "[REDACTED]",
            phoneNumber = null,
            // Withdraw all consent
            privacyConsent = PrivacyConsent(
                marketingConsent = false,
                analyticsConsent = false,
                thirdPartyDataSharingConsent = false,
                consentTimestamp = System.currentTimeMillis()
            ),
            // Keep business unit reference for system integrity
            // But mark as deleted with timestamp
            businessUnitName = "[DELETED-${System.currentTimeMillis()}]",
            lastSeenAt = System.currentTimeMillis(),
            userStatus = UserStatus.INACTIVE
        )
        
        val savedUser = userRepository.save(anonymizedUser)
        
        // Log for audit trail
        logger.info("User $userId anonymized due to GDPR right to erasure request")
        
        return savedUser.toDto()
    }

    /**
     * Update a user's status (active/inactive/suspended)
     * 
     * @param userId The ID of the user
     * @param status The new status to set
     * @return The updated user data
     */
    suspend fun updateUserStatus(userId: String, status: UserStatus): UserDto {
        val user = userRepository.findById(userId)
            ?: throw NoSuchElementException("User not found with ID: $userId")
        
        val updatedUser = user.copy(
            userStatus = status,
            lastSeenAt = System.currentTimeMillis() // Update timestamp when status changes
        )
        
        // Log status change for audit purposes
        logger.info("Updated status for user $userId from ${user.userStatus} to $status")
        
        return userRepository.save(updatedUser).toDto()
    }
    
    /**
     * Get all users with a specific status
     * 
     * @param status The status to filter by
     * @return Flow of users with the specified status
     */
    fun getUsersByStatus(status: UserStatus): Flow<UserDto> {
        return userRepository.findAllByUserStatus(status).map { it.toDto() }
    }
}