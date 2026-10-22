package com.example.cst438project2.backend.service

import com.example.cst438project2.backend.dto.CategoryRequest
import com.example.cst438project2.backend.dto.CategoryResponse
import com.example.cst438project2.backend.dto.toResponse
import com.example.cst438project2.backend.entity.Category
import com.example.cst438project2.backend.repository.CategoryRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class CategoryService(private val categoryRepository: CategoryRepository) {
    fun getAll(): List<CategoryResponse> = categoryRepository.findAll().map { it.toResponse() }

    @Transactional
    fun create(request: CategoryRequest): CategoryResponse {
        val name = request.name.trim()
        if (categoryRepository.findByNameIgnoreCase(name) != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Category already exists")
        }
        return try {
            categoryRepository.save(Category(name = name)).toResponse()
        } catch (_: DataIntegrityViolationException) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Category already exists")
        }
    }

    @Transactional
    fun delete(id: Long) {
        if (!categoryRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found")
        }
        try {
            categoryRepository.deleteById(id)
        } catch (_: DataIntegrityViolationException) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Category is used by an event")
        }
    }
}
