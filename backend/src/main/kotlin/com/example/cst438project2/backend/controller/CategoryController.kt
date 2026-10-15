package com.example.cst438project2.backend.controller

import com.example.cst438project2.backend.entity.Category
import com.example.cst438project2.backend.repository.CategoryRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/categories")
class CategoryController(
    private val categoryRepository: CategoryRepository
) {
    @GetMapping
    fun getCategories(): List<Category> {
        return categoryRepository.findAll()
    }
}