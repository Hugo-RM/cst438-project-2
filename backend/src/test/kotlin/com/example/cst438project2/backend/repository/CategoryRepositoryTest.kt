package com.example.cst438project2.backend.repository

import com.example.cst438project2.backend.entity.Category
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.dao.DataIntegrityViolationException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

@RepositoryTest
class CategoryRepositoryTest @Autowired constructor(
    val categoryRepository: CategoryRepository,
    val entityManager: TestEntityManager,
) {

    @Test
    fun `saves a category and reads it back`() {
        val saved = categoryRepository.save(Category(name = "Music"))
        entityManager.flush()
        entityManager.clear()

        assertEquals("Music", categoryRepository.findById(saved.id!!).orElseThrow().name)
    }

    @Test
    fun `findAll returns every category`() {
        categoryRepository.saveAll(listOf(Category(name = "Music"), Category(name = "Sports")))
        entityManager.flush()

        assertEquals(setOf("Music", "Sports"), categoryRepository.findAll().map { it.name }.toSet())
    }

    @Test
    fun `category names must be unique`() {
        categoryRepository.saveAndFlush(Category(name = "Music"))

        assertFailsWith<DataIntegrityViolationException> {
            categoryRepository.saveAndFlush(Category(name = "Music"))
        }
    }

    @Test
    fun `renames a category`() {
        val saved = categoryRepository.saveAndFlush(Category(name = "Musc"))

        saved.name = "Music"
        categoryRepository.saveAndFlush(saved)
        entityManager.clear()

        assertEquals("Music", categoryRepository.findById(saved.id!!).orElseThrow().name)
    }

    @Test
    fun `deletes a category`() {
        val saved = categoryRepository.saveAndFlush(Category(name = "Music"))

        categoryRepository.deleteById(saved.id!!)
        entityManager.flush()

        assertFalse(categoryRepository.existsById(saved.id!!))
    }
}
