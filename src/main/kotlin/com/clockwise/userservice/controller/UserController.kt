package com.clockwise.userservice.controller

import com.clockwise.userservice.service.CreateUserRequest
import com.clockwise.userservice.service.UpdateUserRequest
import com.clockwise.userservice.service.UserDto
import com.clockwise.userservice.service.UserService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/v1/users")
class UserController(private val userService: UserService) {

    @PostMapping
//    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    suspend fun createUser(@RequestBody request: CreateUserRequest): ResponseEntity<UserDto>  {
        val user = userService.createUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(user)
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    suspend fun getUserById(@PathVariable id: String): ResponseEntity<UserDto> {
        println("NAANANANANNA")
        return ResponseEntity.ok(userService.getUserById(id))
    }

    @GetMapping("/me")
    suspend fun getCurrentUser(): ResponseEntity<UserDto> {
        val username = ReactiveSecurityContextHolder.getContext()
            .map { context: SecurityContext -> context.authentication.name }
            .block() ?: throw IllegalStateException("No authenticated user found")

        return ResponseEntity.ok(userService.getUserByUsername(username))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    suspend fun updateUser(
        @PathVariable id: UUID,
        @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserDto> {
        return ResponseEntity.ok(userService.updateUser(id, request))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    suspend fun deleteUser(@PathVariable id: UUID): ResponseEntity<Void> {
        userService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    fun getUsersByRestaurantId(@PathVariable restaurantId: UUID): Flow<UserDto> {
        return userService.getUsersByRestaurantId(restaurantId)
    }
}