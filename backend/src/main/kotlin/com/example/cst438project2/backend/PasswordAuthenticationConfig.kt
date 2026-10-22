package com.example.cst438project2.backend

import com.example.cst438project2.backend.repository.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class PasswordAuthenticationConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(12)
    }

    @Bean
    fun userDetailsService(
        userRepository: UserRepository
    ): UserDetailsService {
        return UserDetailsService { username ->
            val account = userRepository.findByUsername(username)
                ?: throw UsernameNotFoundException(
                    "Invalid credentials"
                )

            val passwordHash = account.passwordHash
                ?: throw UsernameNotFoundException(
                    "Invalid credentials"
                )

            User.withUsername(requireNotNull(account.username))
                .password(passwordHash)
                .roles(if (account.isAdmin) "ADMIN" else "USER")
                .build()
        }
    }
}