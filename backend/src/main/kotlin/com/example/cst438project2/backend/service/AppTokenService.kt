
package com.example.cst438project2.backend.service

import com.example.cst438project2.backend.entity.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class AppTokenService(
    private val jwtEncoder: JwtEncoder
) {
    fun createToken(user: User): String {
        val now = Instant.now()

        val claims = JwtClaimsSet.builder()
            .issuer("eventhub-app")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(3600))
            .subject(requireNotNull(user.id).toString())
            .claim("auth_type", "password")
            .build()

        val header = JwsHeader.with(MacAlgorithm.HS256).build()

        return jwtEncoder.encode(
            JwtEncoderParameters.from(header, claims)
        ).tokenValue
    }
}
