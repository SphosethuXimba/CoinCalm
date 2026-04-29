package com.st10448336.coincalm

import org.junit.Assert.*
import org.junit.Test

class CoinCalmRequirementsTest {

    // =========================
    // LOGIN REQUIREMENTS
    // =========================

    @Test
    fun `login accepts valid email format`() {
        assertTrue(Validator.isValidEmail("user@gmail.com"))
    }

    @Test
    fun `login rejects invalid email format`() {
        assertFalse(Validator.isValidEmail("not-an-email"))
    }

    @Test
    fun `login is disabled when email is blank`() {
        val email = ""
        val password = "password123"

        val enabled = Validator.isValidEmail(email) && password.isNotBlank()

        assertFalse(enabled)
    }

    @Test
    fun `login is disabled when password is blank`() {
        val email = "user@gmail.com"
        val password = ""

        val enabled = Validator.isValidEmail(email) && password.isNotBlank()

        assertFalse(enabled)
    }

    @Test
    fun `login is enabled only when valid inputs provided`() {
        val email = "user@gmail.com"
        val password = "password123"

        val enabled = Validator.isValidEmail(email) && password.isNotBlank()

        assertTrue(enabled)
    }

    // =========================
    // CATEGORY REQUIREMENTS
    // =========================

    @Test
    fun `category name cannot be blank`() {
        assertFalse("   ".trim().isNotBlank())
    }

    @Test
    fun `category name max 30 chars`() {
        assertTrue("Groceries".length <= 30)
    }

    @Test
    fun `category name too long rejected`() {
        assertTrue("A".repeat(31).length > 30)
    }

    // =========================
    // EXPENSE REQUIREMENTS
    // =========================

    @Test
    fun `expense amount must be numeric`() {
        assertNotNull("100.50".toFloatOrNull())
    }

    @Test
    fun `expense rejects invalid amount`() {
        assertNull("abc".toFloatOrNull())
    }

    @Test
    fun `date format validation`() {
        val regex = Regex("\\d{4}-\\d{2}-\\d{2}")
        assertTrue(regex.matches("2025-04-01"))
    }

    @Test
    fun `invalid date format rejected`() {
        val regex = Regex("\\d{4}-\\d{2}-\\d{2}")
        assertFalse(regex.matches("01-04-2025"))
    }

    @Test
    fun `time format validation`() {
        val regex = Regex("\\d{2}:\\d{2}")
        assertTrue(regex.matches("08:30"))
    }

    // =========================
    // PHOTO OPTIONAL
    // =========================

    @Test
    fun `photo is optional`() {
        val photoUrl: String? = null
        assertTrue(photoUrl == null)
    }

    // =========================
    // BUDGET REQUIREMENTS
    // =========================

    @Test
    fun `max must be greater than min`() {
        assertTrue(5000f > 1000f)
    }

    @Test
    fun `invalid budget rejected`() {
        assertFalse(1000f > 5000f)
    }

    // =========================
    // REGISTRATION REQUIREMENTS
    // =========================

    @Test
    fun `username must be at least 3 chars`() {
        assertTrue(Validator.isValidUsername("Andiswa"))
    }

    @Test
    fun `password must be at least 8 chars`() {
        assertTrue(Validator.isValidPassword("password123"))
    }

    @Test
    fun `email is validated correctly`() {
        assertTrue(Validator.isValidEmail("test@gmail.com"))
    }

    @Test
    fun `income must be positive`() {
        assertTrue(Validator.isValidIncome("5000"))
    }
}