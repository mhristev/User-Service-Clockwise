package com.clockwise.userservice.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("refresh_tokens")
data class RefreshToken(
    @Id
    val id: String? = null,
    val token: String,
    val userId: String,
    val expiryDate: Long,
    val isRevoked: Boolean = false
)