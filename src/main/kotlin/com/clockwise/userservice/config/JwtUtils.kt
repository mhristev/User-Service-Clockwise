package com.clockwise.userservice.config

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Component
class JwtUtils(
    @Value("\${security.jwt.secret}")
    private val jwtSecret: String,

    @Value("\${security.jwt.expiration}")
    private val jwtExpirationMs: Long,

    @Value("\${security.jwt.refresh-expiration}")
    private val refreshExpirationMs: Long
) {
   // private val key: Key = Keys.secretKeyFor(SignatureAlgorithm.HS512)
    private val key: Key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())

    fun generateToken(userDetails: UserDetails): String {
        val claims = HashMap<String, Any>()
        claims["roles"] = userDetails.authorities.map { it.authority } // Should be "ROLE_ADMIN", etc.

//        claims["roles"] = userDetails.authorities.map { it.authority.let { role ->
//            if (!role.startsWith("ROLE_")) "ROLE_$role" else role
//        } }

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.username)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    fun generateRefreshToken(userDetails: UserDetails): String {
        return Jwts.builder()
            .setSubject(userDetails.username)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + refreshExpirationMs))
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = extractAllClaims(token)
            !isTokenExpired(claims)
        } catch (ex: Exception) {
            false
        }
    }

    fun getAuthentication(token: String): Authentication {
        val claims = extractAllClaims(token)
        val username = claims.subject

        val roles = when (val rolesObj = claims["roles"]) {
            is List<*> -> rolesObj.filterIsInstance<String>()
            else -> emptyList()
        }

        val authorities = roles.takeIf { it.isNotEmpty() }
            ?.map { SimpleGrantedAuthority(it) }
            ?: listOf(SimpleGrantedAuthority("ROLE_USER"))

        return UsernamePasswordAuthenticationToken(username, "", authorities)
    }

    fun getUsername(token: String): String {
        return extractAllClaims(token).subject
    }

    fun getExpirationDate(token: String): Date {
        return extractAllClaims(token).expiration
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
    }

    private fun isTokenExpired(claims: Claims): Boolean {
        return claims.expiration.before(Date())
    }
}