package com.clockwise.userservice.controller

import com.clockwise.userservice.service.*
import com.clockwise.userservice.domain.UserStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

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
//    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
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
        @PathVariable id: String,
        @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserDto> {
        return ResponseEntity.ok(userService.updateUser(id, request))
    }

    @PutMapping("/{id}/business-unit")
    suspend fun updateUserBusinessUnit(
        @PathVariable id: String,
        @RequestBody request: UpdateBusinessUnitRequest
    ): ResponseEntity<UserDto> {
        return ResponseEntity.ok(userService.updateUserBusinessUnit(id, request))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    suspend fun deleteUser(@PathVariable id: UUID): ResponseEntity<Void> {
        userService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }
    private val logger = LoggerFactory.getLogger(UserController::class.java)
    @GetMapping("/restaurant/{restaurantId}")
//    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    fun getUsersByRestaurantId(@PathVariable restaurantId: String): Flow<UserDto> {
        return userService.getUsersByRestaurantId(restaurantId)
    }

    @GetMapping("/without-business-unit")
    suspend fun getUsersWithoutBusinessUnit(
        @RequestParam(required = false) email: String?
    ): Flow<UserDto> = coroutineScope {
        logger.info("Getting users without business unit")
        userService.getUsersWithoutBusinessUnit(email)
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    suspend fun updateUserStatus(
        @PathVariable id: String,
        @RequestBody request: UpdateUserStatusRequest
    ): ResponseEntity<UserDto> {
        val user = userService.updateUserStatus(id, request.status)
        return ResponseEntity.ok(user)
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('ADMIN')")
    fun getUsersByStatus(
        @PathVariable status: UserStatus
    ): Flow<UserDto> {
        return userService.getUsersByStatus(status)
    }
}