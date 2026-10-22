package com.example.cst438project2.backend

import com.example.cst438project2.backend.entity.Category
import com.example.cst438project2.backend.repository.CategoryRepository
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CategoryMaker {
    @Bean
    fun makingCategories(categoryRepository: CategoryRepository) =
        ApplicationRunner {
            val defaultCategories = listOf(
                "Academic",
                "Social",
                "Sports",
                "Career",
                "Other"
            )

            for (name in defaultCategories) {
                if (categoryRepository.findByNameIgnoreCase(name) == null) {
                    categoryRepository.save(Category(name = name))
                }
            }
        }
}