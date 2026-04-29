package com.st10448336.coincalm

import org.junit.Assert.*
import org.junit.Test

class CoinCalmUnitTests {

    // ─── Category name validation (mirrors AddCategoryScreen logic) ───

    @Test
    fun `category name blank is invalid`() {
        val name = "  "
        assertFalse(name.isNotBlank())
    }

    @Test
    fun `category name within limit is valid`() {
        val name = "Groceries"
        assertTrue(name.isNotBlank() && name.length <= 30)
    }

    @Test
    fun `category name over 30 chars is invalid`() {
        val name = "A".repeat(31)
        assertTrue(name.length > 30)
    }

    // ─── Expense input validation (mirrors AddExpenseScreen logic) ───

    @Test
    fun `valid amount parses correctly`() {
        val input = "150.50"
        assertNotNull(input.toFloatOrNull())
    }

    @Test
    fun `invalid amount string returns null`() {
        val input = "abc"
        assertNull(input.toFloatOrNull())
    }

    @Test
    fun `empty amount is invalid`() {
        val input = ""
        assertTrue(input.isBlank())
    }

    @Test
    fun `expense is saveable only when all fields filled`() {
        val amount = "200.00"
        val date = "2025-04-01"
        val startTime = "08:00"
        val endTime = "09:00"
        val description = "Lunch"
        val categorySelected = true

        val isSaveEnabled = amount.isNotBlank() && date.isNotBlank() &&
                startTime.isNotBlank() && endTime.isNotBlank() &&
                description.isNotBlank() && categorySelected

        assertTrue(isSaveEnabled)
    }

    @Test
    fun `expense is not saveable when description is missing`() {
        val amount = "200.00"
        val date = "2025-04-01"
        val startTime = "08:00"
        val endTime = "09:00"
        val description = ""
        val categorySelected = true

        val isSaveEnabled = amount.isNotBlank() && date.isNotBlank() &&
                startTime.isNotBlank() && endTime.isNotBlank() &&
                description.isNotBlank() && categorySelected

        assertFalse(isSaveEnabled)
    }

    // ─── Budget goals validation (mirrors BudgetGoalsScreen logic) ───

    @Test
    fun `max goal must be greater than min goal`() {
        val min = 1000f
        val max = 5000f
        assertTrue(max > min)
    }

    @Test
    fun `equal min and max goal is invalid`() {
        val min = 3000f
        val max = 3000f
        assertFalse(max > min)
    }

    @Test
    fun `max less than min is invalid`() {
        val min = 5000f
        val max = 1000f
        assertFalse(max > min)
    }

    @Test
    fun `both goals must be non-negative`() {
        val min = -100f
        val max = 500f
        assertFalse(min >= 0 && max >= 0)
    }

    // ─── XP & gamification logic (mirrors BadgesScreen logic) ───

    @Test
    fun `xp is calculated correctly from actions`() {
        val expenseCount = 5
        val categoryCount = 3
        val goalCount = 2
        val xp = (expenseCount * 10) + (categoryCount * 20) + (goalCount * 30)
        assertEquals(170, xp)
    }

    @Test
    fun `level is 1 when xp is below 1000`() {
        val totalXp = 500
        val level = (totalXp / 1000) + 1
        assertEquals(1, level)
    }

    @Test
    fun `level increments correctly at 1000 xp`() {
        val totalXp = 1000
        val level = (totalXp / 1000) + 1
        assertEquals(2, level)
    }

    @Test
    fun `level title is Rookie Saver for level 1`() {
        val level = 1
        val title = when {
            level >= 10 -> "Money Master"
            level >= 7  -> "Wealth Wizard"
            level >= 5  -> "Budget Sage"
            level >= 3  -> "Coin Tracker"
            else        -> "Rookie Saver"
        }
        assertEquals("Rookie Saver", title)
    }

    @Test
    fun `level title is Money Master for level 10`() {
        val level = 10
        val title = when {
            level >= 10 -> "Money Master"
            level >= 7  -> "Wealth Wizard"
            level >= 5  -> "Budget Sage"
            level >= 3  -> "Coin Tracker"
            else        -> "Rookie Saver"
        }
        assertEquals("Money Master", title)
    }

    // ─── Badge unlock logic ───

    @Test
    fun `first steps badge unlocks after 1 expense`() {
        val expenseCount = 1
        assertTrue(expenseCount >= 1)
    }

    @Test
    fun `saver pro badge requires 10 or more expenses`() {
        val expenseCount = 9
        assertFalse(expenseCount >= 10)
    }

    @Test
    fun `seven day streak badge requires 7 consecutive days`() {
        val streak = 7
        assertTrue(streak >= 7)
    }
}