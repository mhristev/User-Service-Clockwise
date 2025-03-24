package com.clockwise.userservice.config

import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class JwtAuthConverter(private val jwtUtils: JwtUtils) : ServerAuthenticationConverter {

    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        return Mono.justOrEmpty(exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION))
            .filter { authHeader -> authHeader.startsWith("Bearer ") }
            .map { authHeader -> authHeader.substring(7) }
            .flatMap { token ->
                try {
                    if (jwtUtils.validateToken(token)) {
                        Mono.just(jwtUtils.getAuthentication(token))
                    } else {
                        Mono.empty()
                    }
                }catch (e: Exception) {
                    println(e.localizedMessage)
                    Mono.empty()
                }
            }
    }
}

@Component
class JwtAuthManager : ReactiveAuthenticationManager {

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        return Mono.just(authentication)
            .map { auth ->
                val authorities = auth.authorities
                if (authorities.isEmpty()) {
                    throw RuntimeException("No authorities found")
                }
                UsernamePasswordAuthenticationToken(auth.principal, auth.credentials, authorities)
            }
    }
}