package com.clockwise.userservice.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

enum class UserRole {
    ADMIN, MANAGER, EMPLOYEE
}

@Table("users")
data class User(
    @Id
    val id: String? = null,
    val username: String,
    val email: String,
    val password: String,
    val role: UserRole,
    val restaurantId: String? = null
)