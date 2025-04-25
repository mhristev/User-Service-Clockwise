package com.clockwise.userservice.config

import com.clockwise.userservice.service.UserService
import kotlinx.coroutines.runBlocking
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * Filter to update user's lastSeenAt timestamp on authenticated requests
 * This is important for GDPR compliance to track user activity for data retention
 */
@Component
class UserActivityFilter(private val userService: UserService) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return chain.filter(exchange)
            .contextWrite { context ->
                if (context.hasKey("securityContext")) {
                    ReactiveSecurityContextHolder.getContext()
                        .doOnNext { securityContext ->
                            val username = securityContext.authentication.name
                            if (username != "anonymousUser") {
                                // Update last activity timestamp in a separate thread to avoid blocking
                                try {
                                    runBlocking {
                                        try {
                                            val user = userService.getUserByEmail(username)
                                            user.id?.let { userId ->
                                                userService.updateLastSeen(userId)
                                            }
                                        } catch (e: Exception) {
                                            // User might not exist, log and continue
                                            println("User not found or error updating last seen: ${e.message}")
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Log but don't interrupt the request flow
                                    println("Failed to update last seen timestamp: ${e.message}")
                                }
                            }
                        }
                        .subscribe()
                    context
                } else {
                    context
                }
            }
    }
} 