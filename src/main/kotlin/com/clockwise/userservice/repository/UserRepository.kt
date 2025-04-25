package com.clockwise.userservice.repository

import com.clockwise.userservice.domain.User
import com.clockwise.userservice.domain.UserStatus
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*
import kotlinx.coroutines.flow.Flow

@Repository
interface UserRepository : CoroutineCrudRepository<User, UUID> {
    suspend fun findByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE email = :username")
    suspend fun findByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE business_unit_id = :restaurantId")
    fun findAllByBusinessUnitId(restaurantId: String): Flow<User>

    @Query("SELECT COUNT(*) > 0 FROM users WHERE email = :email")
    suspend fun existsByEmail(email: String): Boolean

    @Query("SELECT COUNT(*) > 0 FROM users WHERE LOWER(email) = LOWER(:email)")
    suspend fun existsByEmailIgnoreCase(email: String): Boolean

    suspend fun findById(id: String): User?

    @Query("SELECT * FROM users WHERE business_unit_id IS NULL AND (:email IS NULL OR email = :email)")
    fun findAllByBusinessUnitIdIsNullAndEmail(email: String? = null): Flow<User>

    @Query("SELECT * FROM users WHERE LOWER(first_name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(last_name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    fun findByNameContaining(searchTerm: String): Flow<User>

    suspend fun findByPhoneNumber(phoneNumber: String): User?

    @Query("SELECT * FROM users ORDER BY last_seen_at DESC LIMIT :limit")
    fun findRecentlyActiveUsers(limit: Int): Flow<User>

    @Query("UPDATE users SET last_seen_at = :timestamp WHERE id = :userId RETURNING *")
    suspend fun updateLastSeenAt(userId: String, timestamp: Long): User?

    @Query("SELECT * FROM users WHERE user_status = :status")
    fun findAllByUserStatus(status: UserStatus): Flow<User>
}