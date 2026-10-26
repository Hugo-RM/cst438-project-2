package com.example.cst438project2.backend

import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jose.jwk.source.ImmutableSecret
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.web.SecurityFilterChain
import java.util.Base64
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Configuration
class SecurityConfig(
    @Value("\${app.google.client-id}")
    private val googleClientId: String,
    @Value("\${app.auth.token-secret}")
    private val tokenSecret: String
) {

    private fun signingKey(): SecretKey {
        val bytes = Base64.getDecoder().decode(tokenSecret)
        require(bytes.size >= 32) {
            "APP_TOKEN_SECRET must decode to at least 32 bytes"
        }
        return SecretKeySpec(bytes, "HmacSHA256")
    }

    @Bean
    fun jwtEncoder(): JwtEncoder {
        val source = ImmutableSecret<SecurityContext>(signingKey())
        return NimbusJwtEncoder(source)
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val appDecoder = NimbusJwtDecoder
            .withSecretKey(signingKey())
            .macAlgorithm(MacAlgorithm.HS256)
            .build()

        appDecoder.setJwtValidator(
            JwtValidators.createDefaultWithIssuer("eventhub-app")
        )

        val googleDecoder = JwtDecoders.fromIssuerLocation(
            "https://accounts.google.com"
        ) as NimbusJwtDecoder

        val issuerValidator =
            JwtValidators.createDefaultWithIssuer("https://accounts.google.com")

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

        googleDecoder.setJwtValidator(
            DelegatingOAuth2TokenValidator(
                issuerValidator,
                audienceValidator
            )
        )

        return JwtDecoder { token ->
            try {
                appDecoder.decode(token)
            } catch (appError: JwtException) {
                try {
                    googleDecoder.decode(token)
                } catch (googleError: JwtException) {
                    throw googleError
                }
            }
        }
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/actuator/health").permitAll()
                    // Swagger UI and the generated spec are public; /error lets 404s and 400s through unchanged
                    .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
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
            .sessionManagement {
                it.sessionCreationPolicy(
                    org.springframework.security.config.http.SessionCreationPolicy.STATELESS
                )
            }
            .httpBasic {
                it.authenticationEntryPoint(
                    org.springframework.security.web.authentication.HttpStatusEntryPoint(
                        org.springframework.http.HttpStatus.UNAUTHORIZED
                    )
                )
            }
            .oauth2ResourceServer { it.jwt {} }

        return http.build()
    }
}