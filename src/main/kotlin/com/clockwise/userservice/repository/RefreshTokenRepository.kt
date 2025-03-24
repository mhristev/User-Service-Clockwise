package com.clockwise.userservice.repository

import com.clockwise.userservice.domain.RefreshToken
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RefreshTokenRepository : CoroutineCrudRepository<RefreshToken, UUID> {
    suspend fun findByToken(token: String): RefreshToken?

    suspend fun findByUserId(userId: String): RefreshToken?

    @Modifying
    @Query("UPDATE refresh_tokens SET is_revoked = true WHERE user_id = :userId")
    suspend fun revokeAllUserTokens(userId: String)

    @Modifying
    @Query("DELETE FROM refresh_tokens WHERE expiry_date < :now")
    suspend fun deleteExpiredTokens(now: Long)
}