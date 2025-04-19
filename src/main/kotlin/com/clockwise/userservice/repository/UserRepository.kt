package com.clockwise.userservice.repository

import com.clockwise.userservice.domain.User
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*
import kotlinx.coroutines.flow.Flow

@Repository
interface UserRepository : CoroutineCrudRepository<User, UUID> {
    suspend fun findByUsername(username: String): User?
    suspend fun findByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE business_unit_id = :restaurantId")
    fun findAllByBusinessUnitId(restaurantId: String): Flow<User>

    @Query("SELECT COUNT(*) > 0 FROM users WHERE email = :email")
    suspend fun existsByEmail(email: String): Boolean

    @Query("SELECT COUNT(*) > 0 FROM users WHERE username = :username")
    suspend fun existsByUsername(username: String): Boolean

    suspend fun findById(id: String): User?

    @Query("SELECT * FROM users WHERE business_unit_id IS NULL AND (:email IS NULL OR email = :email)")
    fun findAllByBusinessUnitIdIsNullAndEmail(email: String? = null): Flow<User>
}