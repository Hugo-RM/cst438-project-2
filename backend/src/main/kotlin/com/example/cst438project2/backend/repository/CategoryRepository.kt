package com.example.cst438project2.backend.repository

import com.example.cst438project2.backend.entity.Category
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<Category, Long>