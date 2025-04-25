package com.clockwise.userservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import reactor.core.publisher.Mono
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.web.server.WebFilterExchange

@Configuration
@EnableWebFluxSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthManager: JwtAuthManager,
    private val jwtAuthConverter: JwtAuthConverter
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        val authFilter = AuthenticationWebFilter(jwtAuthManager).apply {
            setServerAuthenticationConverter(jwtAuthConverter)
            setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/v1/**"))

//            setAuthenticationSuccessHandler { webFilterExchange, authentication ->
//                WebFilterExchange.chain.filter
////                Mono.fromCallable { ReactiveSecurityContextHolder.withAuthentication(authentication) }
////                    .then(webFilterExchange.chain.filter(webFilterExchange.exchange))
//            }
            setAuthenticationSuccessHandler { webFilterExchange, authentication ->
                webFilterExchange.chain.filter(webFilterExchange.exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
            }
        }

        return http
            .exceptionHandling {
                it.accessDeniedHandler { exchange, _ ->
                    exchange.response.statusCode = HttpStatus.FORBIDDEN
                    Mono.empty()
                }
                it.authenticationEntryPoint { exchange, _ ->
                    exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                    Mono.empty()
                }
            }
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .authorizeExchange {
                it.pathMatchers(HttpMethod.POST, "/v1/auth/login", "/v1/auth/refresh", "/v1/auth/register").permitAll()
                it.pathMatchers(HttpMethod.GET, "/v1/privacy-policy").permitAll()
                it.pathMatchers(HttpMethod.POST, "/v1/users").hasAnyAuthority("ADMIN","MANAGER")
                it.pathMatchers(HttpMethod.GET, "/v1/users/{id}").permitAll()
                it.pathMatchers(HttpMethod.GET, "/v1/users/me").authenticated()
                it.pathMatchers(HttpMethod.PUT, "/v1/users/{id}").hasAnyRole("ADMIN", "MANAGER")
                it.pathMatchers(HttpMethod.DELETE, "/v1/users/{id}").hasRole("ADMIN")
                it.pathMatchers(HttpMethod.GET, "/v1/without-business-unit").permitAll()
                it.pathMatchers(HttpMethod.PUT, "/v1/users/{id}/business-unit").permitAll()
                
                // GDPR consent endpoints - Each user should only manage their own consent
                it.pathMatchers(HttpMethod.GET, "/v1/users/consent/{userId}").authenticated()
                it.pathMatchers(HttpMethod.PUT, "/v1/users/consent/{userId}").authenticated()
                it.pathMatchers(HttpMethod.DELETE, "/v1/users/consent/{userId}").authenticated()
                it.pathMatchers(HttpMethod.PUT, "/v1/users/consent/{userId}/retention").hasAnyRole("ADMIN")
                
                // GDPR rights endpoints
                it.pathMatchers(HttpMethod.DELETE, "/v1/gdpr/erase-me").authenticated() // Personal right to erasure
                it.pathMatchers(HttpMethod.DELETE, "/v1/gdpr/users/{userId}/erase").hasRole("ADMIN") // Admin function
                it.pathMatchers(HttpMethod.GET, "/v1/gdpr/my-data").authenticated() // Personal right to access
                
                it.anyExchange().permitAll()
            }
            .addFilterAt(authFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .build()
    }
}