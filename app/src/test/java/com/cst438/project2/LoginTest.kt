package com.cst438.project2

import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse

class LoginTest {

    @Test
    fun correctUsernameAndPassword() {
        val username = "Justin"
        val password = "hello"

        val result = username == "Justin" && password == "hello"
        assertTrue(result)
    }

    @Test
    fun incorrectPassword() {
        val username = "Justin"
        val password = "wrong"

        val result = username == "Justin" && password == "hello"
        assertFalse(result)
    }

    @Test
    fun incorrectUsername() {
        val username = "wrong"
        val password = "hello"

        val result = username == "Justin" && password == "hello"
        assertFalse(result)
    }
}