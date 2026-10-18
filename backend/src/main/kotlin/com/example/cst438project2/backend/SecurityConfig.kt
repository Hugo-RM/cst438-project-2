package com.example.cst438project2.backend

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.web.SecurityFilterChain
import org.springframework.http.HttpMethod

@Configuration
class SecurityConfig(
    @Value("\${app.google.client-id}")
    private val googleClientId: String
) {
    @Bean
    fun jwtDecoder(): JwtDecoder {
        val decoder = JwtDecoders.fromIssuerLocation(
            "https://accounts.google.com"
        ) as NimbusJwtDecoder

        val issuerValidator =
            JwtValidators.createDefaultWithIssuer(
                "https://accounts.google.com"
            )

        val audienceValidator = OAuth2TokenValidator<Jwt> { jwt ->
            if (jwt.audience?.contains(googleClientId) == true) {
                OAuth2TokenValidatorResult.success()
            } else {
                OAuth2TokenValidatorResult.failure(
                    OAuth2Error(
                        "invalid_token",
                        "Invalid Google token audience",
                        null
                    )
                )
            }
        }

        decoder.setJwtValidator(
            DelegatingOAuth2TokenValidator(
                issuerValidator,
                audienceValidator
            )
        )

        return decoder
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/health"
                    ).permitAll()
                    .requestMatchers(
                        "/api/v1/auth/**"
                    ).permitAll()
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/events",
                        "/api/v1/events/*",
                        "/api/v1/categories"
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { it.jwt {} }

        return http.build()
    }
}