package com.st10448336.coincalm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.st10448336.coincalm.data.AppDatabase
import com.st10448336.coincalm.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.draw.alpha

// ── Data model for a badge ─────────────────────────────────────────────────────
data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val isEarned: Boolean,
    val xpReward: Int
)

@Composable
fun BadgesScreen(navController: NavController) {

    val context = LocalContext.current
    val auth    = FirebaseAuth.getInstance()

    var totalExpenseCount by remember { mutableIntStateOf(0) }
    var categoryCount     by remember { mutableIntStateOf(0) }
    var goalCount         by remember { mutableIntStateOf(0) }
    var consecutiveDays   by remember { mutableIntStateOf(0) }
    var totalXpEarned     by remember { mutableIntStateOf(0) }

    // ── Load stats from Room ───────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)

            val expenses = db.expenseDao().getAllExpensesForUser(uid)
            val cats     = db.categoryDao().getCategoriesForUser(uid).first()
            val goals    = db.goalDao().getAllGoalsForUser(uid)

            // ── Consecutive-day streak ─────────────────────────────────────
            val sdf           = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val distinctDates = expenses
                .map { it.date }
                .distinct()
                .sortedDescending()

            var streak = 0
            val cal    = Calendar.getInstance()
            for (dateStr in distinctDates) {
                val expected = sdf.format(cal.time)
                if (dateStr == expected) {
                    streak++
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                } else break
            }

            withContext(Dispatchers.Main) {
                totalExpenseCount = expenses.size
                categoryCount     = cats.size
                goalCount         = goals.size
                consecutiveDays   = streak
                totalXpEarned     = (expenses.size * 10) + (cats.size * 20) + (goals.size * 30)
            }
        }
    }

    // ── XP & Level calculation ─────────────────────────────────────────────────
    val xpPerLevel   = 1000
    val currentLevel = (totalXpEarned / xpPerLevel) + 1
    val xpIntoLevel  = totalXpEarned % xpPerLevel
    val xpProgress   = xpIntoLevel.toFloat() / xpPerLevel.toFloat()

    val levelTitle = when {
        currentLevel >= 10 -> "Money Master"
        currentLevel >= 7  -> "Wealth Wizard"
        currentLevel >= 5  -> "Budget Sage"
        currentLevel >= 3  -> "Coin Tracker"
        else               -> "Rookie Saver"
    }

    // ── Badge definitions ──────────────────────────────────────────────────────
    val badges = listOf(
        Badge(
            id          = "first_steps",
            title       = "First Steps",
            description = "Log your first expense",
            emoji       = "🥇",
            isEarned    = totalExpenseCount >= 1,
            xpReward    = 50
        ),
        Badge(
            id          = "seven_day_streak",
            title       = "7-Day Streak",
            description = "Log expenses 7 days in a row",
            emoji       = "🔥",
            isEarned    = consecutiveDays >= 7,
            xpReward    = 200
        ),
        Badge(
            id          = "saver_pro",
            title       = "Saver Pro",
            description = "Log 10 or more expenses",
            emoji       = "💰",
            isEarned    = totalExpenseCount >= 10,
            xpReward    = 150
        ),
        Badge(
            id          = "budget_master",
            title       = "Budget Master",
            description = "Create 5 or more budget goals",
            emoji       = "🏆",
            isEarned    = goalCount >= 5,
            xpReward    = 300
        ),
        Badge(
            id          = "goal_crusher",
            title       = "Goal Crusher",
            description = "Create your first savings goal",
            emoji       = "🎯",
            isEarned    = goalCount >= 1,
            xpReward    = 100
        ),
        Badge(
            id          = "team_player",
            title       = "Team Player",
            description = "Set up 3 or more categories",
            emoji       = "👥",
            isEarned    = categoryCount >= 3,
            xpReward    = 100
        ),
        Badge(
            id          = "big_spender",
            title       = "Big Spender",
            description = "Log 25 or more expenses",
            emoji       = "💸",
            isEarned    = totalExpenseCount >= 25,
            xpReward    = 250
        ),
        Badge(
            id          = "organised",
            title       = "Organised",
            description = "Create 5 or more categories",
            emoji       = "📂",
            isEarned    = categoryCount >= 5,
            xpReward    = 150
        )
    )

    val earnedBadges = badges.filter { it.isEarned }
    val lockedBadges = badges.filter { !it.isEarned }

    // ── UI ─────────────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
            .verticalScroll(rememberScrollState())
    ) {

        // ── Top bar ───────────────────────────────────────────────────────
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 20.dp, top = 40.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint               = TextPrimary
                )
            }
            Text(
                text       = "My Achievements",
                color      = TextPrimary,
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // ── Level card ────────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape  = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NavyMedium)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier         = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD700)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = "$currentLevel",
                            color      = NavyDark,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 22.sp
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = "Level $currentLevel — $levelTitle",
                            color      = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold,
                            fontSize   = 16.sp
                        )
                        Text(
                            text     = "$xpIntoLevel / $xpPerLevel XP to next level",
                            color    = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        text       = "$totalXpEarned XP",
                        color      = LimeGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 13.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                // XP progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(NavyLight)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(xpProgress.coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFFFFD700))
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Earned badges ─────────────────────────────────────────────────
        if (earnedBadges.isNotEmpty()) {
            Text(
                text          = "EARNED BADGES",
                color         = TextSecondary,
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 0.1.sp,
                modifier      = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            earnedBadges.chunked(3).forEach { rowBadges ->
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowBadges.forEach { badge ->
                        BadgeCard(badge = badge, modifier = Modifier.weight(1f))
                    }
                    repeat(3 - rowBadges.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Locked badges ─────────────────────────────────────────────────
        if (lockedBadges.isNotEmpty()) {
            Text(
                text          = "LOCKED BADGES",
                color         = TextSecondary,
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 0.1.sp,
                modifier      = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            lockedBadges.chunked(3).forEach { rowBadges ->
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowBadges.forEach { badge ->
                        BadgeCard(badge = badge, modifier = Modifier.weight(1f))
                    }
                    repeat(3 - rowBadges.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ── Single badge card ──────────────────────────────────────────────────────────
@Composable
private fun BadgeCard(
    badge: Badge,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(
            containerColor = if (badge.isEarned) NavyMedium else NavyLight.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier            = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier.alpha(if (badge.isEarned) 1f else 0.35f)
            ) {
                Text(
                    text     = badge.emoji,
                    fontSize = 28.sp
                )
            }
            Text(
                text       = badge.title,
                color      = if (badge.isEarned) TextPrimary else TextSecondary,
                fontSize   = 10.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign  = TextAlign.Center,
                maxLines   = 2
            )
            if (badge.isEarned) {
                Text(
                    text      = "+${badge.xpReward} XP",
                    color     = LimeGreen,
                    fontSize  = 9.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}