package com.example.cst438project2.backend.controller

import com.example.cst438project2.backend.dto.CategoryRequest
import com.example.cst438project2.backend.dto.CategoryResponse
import com.example.cst438project2.backend.service.CategoryService
import com.example.cst438project2.backend.service.CurrentUserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/categories")
class CategoryController(
    private val categoryService: CategoryService,
    private val currentUserService: CurrentUserService
) {
    @GetMapping
    fun getCategories(): List<CategoryResponse> = categoryService.getAll()

    @PostMapping
    fun createCategory(
        @Valid @RequestBody request: CategoryRequest,
        authentication: Authentication
    ): ResponseEntity<CategoryResponse> {
        currentUserService.requireAdmin(currentUserService.getOrCreate(authentication))
        return ResponseEntity.status(201).body(categoryService.create(request))
    }

    @DeleteMapping("/{id}")
    fun deleteCategory(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        currentUserService.requireAdmin(currentUserService.getOrCreate(authentication))
        categoryService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
