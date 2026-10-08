package com.example.cst438project2.backend.repository

import com.example.cst438project2.backend.entity.User
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.dao.DataIntegrityViolationException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RepositoryTest
class UserRepositoryTest @Autowired constructor(
    val userRepository: UserRepository,
    val entityManager: TestEntityManager,
) {

    @Test
    fun `saves a user and reads it back`() {
        val saved = userRepository.save(User(username = "alice", isAdmin = true))
        entityManager.flush()
        entityManager.clear()

        val found = userRepository.findById(saved.id!!).orElseThrow()
        assertEquals("alice", found.username)
        assertTrue(found.isAdmin)
    }

    @Test
    fun `new users are not admins by default`() {
        val saved = userRepository.saveAndFlush(User(username = "bob"))
        entityManager.clear()

        assertFalse(userRepository.findById(saved.id!!).orElseThrow().isAdmin)
    }

    @Test
    fun `findByUsername returns the matching user`() {
        userRepository.saveAndFlush(User(username = "alice"))
        userRepository.saveAndFlush(User(username = "bob"))
        entityManager.clear()

        val found = userRepository.findByUsername("bob")
        assertNotNull(found)
        assertEquals("bob", found.username)
    }

    @Test
    fun `findByUsername returns null when no user matches`() {
        userRepository.saveAndFlush(User(username = "alice"))

        assertNull(userRepository.findByUsername("nobody"))
    }

    @Test
    fun `usernames must be unique`() {
        userRepository.saveAndFlush(User(username = "alice"))

        assertFailsWith<DataIntegrityViolationException> {
            userRepository.saveAndFlush(User(username = "alice"))
        }
    }

    @Test
    fun `deletes a user`() {
        val saved = userRepository.saveAndFlush(User(username = "alice"))

        userRepository.deleteById(saved.id!!)
        entityManager.flush()

        assertFalse(userRepository.existsById(saved.id!!))
    }
}
