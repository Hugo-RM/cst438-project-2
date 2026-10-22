package com.example.cst438project2.backend.repository

import com.example.cst438project2.backend.TestcontainersConfiguration
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import

/**
 * Put this on a repository test class to run it against a real PostgreSQL container.
 * Only the JPA layer is loaded, and each test is rolled back afterwards, so tests
 * don't see each other's data. Inject repositories and TestEntityManager via the constructor.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration::class)
annotation class RepositoryTest
