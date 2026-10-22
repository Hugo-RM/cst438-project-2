package com.example.cst438project2.backend.dto

import com.example.cst438project2.backend.entity.Category
import jakarta.validation.constraints.NotBlank

data class CategoryRequest(
    @field:NotBlank val name: String
)

data class CategoryResponse(
    val id: Long,
    val name: String
)

fun Category.toResponse() = CategoryResponse(
    id = requireNotNull(id),
    name = name
)
