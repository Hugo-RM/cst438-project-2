package com.example.cst438project2.backend

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(TestcontainersConfiguration::class)
class BackendApplicationTests {

	@Test
	fun contextLoads() {
	}

}
