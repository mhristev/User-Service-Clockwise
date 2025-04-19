package com.clockwise.userservice.config
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean

@Configuration
class WebConfig : WebFluxConfigurer {
    @Bean
    fun corsConfigurationSource(): CorsConfiguration {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("http://localhost:5173")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        return configuration
    }

    @Bean
    fun corsWebFilter(): CorsWebFilter {
        val mappings = UrlBasedCorsConfigurationSource()
        mappings.registerCorsConfiguration("/**", corsConfigurationSource())
        return CorsWebFilter(mappings)
    }
}