package com.st10448336.coincalm


object Validator {

    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

    fun isValidEmail(email: String): Boolean {
        return email.trim().isNotBlank() && emailRegex.matches(email.trim())
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    fun isValidUsername(username: String): Boolean {
        return username.trim().length >= 3
    }

    fun isValidIncome(income: String): Boolean {
        return income.toFloatOrNull() != null && income.toFloat() > 0
    }
}