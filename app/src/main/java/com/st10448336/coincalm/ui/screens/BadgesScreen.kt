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

data class Badge(
    val id: String, val title: String, val description: String, val icon: String,
    val iconColor: Color, val isEarned: Boolean, val xpReward: Int
)

/** Calculates user XP and renders the gamification dashboard. */
@Composable
fun BadgesScreen(navController: NavController) {
    val context = LocalContext.current
    val auth    = FirebaseAuth.getInstance()
    var totalExpenseCount by remember { mutableIntStateOf(0) }
    var categoryCount     by remember { mutableIntStateOf(0) }
    var goalCount         by remember { mutableIntStateOf(0) }
    var consecutiveDays   by remember { mutableIntStateOf(0) }
    val isDark = LocalDarkMode.current

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            val expenses = db.expenseDao().getAllExpensesForUser(uid)
            val cats     = db.categoryDao().getCategoriesForUser(uid).first()
            val goals    = db.goalDao().getAllGoalsForUser(uid)

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val distinctDates = expenses.map { it.date }.distinct().sortedDescending()
            var streak = 0
            val cal = Calendar.getInstance()
            for (dateStr in distinctDates) {
                if (dateStr == sdf.format(cal.time)) { streak++; cal.add(Calendar.DAY_OF_YEAR, -1) } else break
            }

            withContext(Dispatchers.Main) {
                totalExpenseCount = expenses.size
                categoryCount     = cats.size
                goalCount         = goals.size
                consecutiveDays   = streak
            }
        }
    }

    val badges = listOf(
        Badge("first_steps", "First Steps", "Log your first expense", "🐾", Color(0xFFFFD700), totalExpenseCount >= 1, 50),
        Badge("seven_day_streak", "7-Day Streak", "Log expenses 7 days in a row", "🔥", Color(0xFFFF6B35), consecutiveDays >= 7, 200),
        Badge("saver_pro", "Saver Pro", "Log 10 or more expenses", "🏆", Color(0xFF4CAF50), totalExpenseCount >= 10, 150),
        Badge("budget_master", "Budget Master", "Create 5 or more budget goals", "👑", Color(0xFF9C27B0), goalCount >= 5, 300),
        Badge("goal_crusher", "Goal Crusher", "Create your first savings goal", "🎯", Color(0xFF2196F3), goalCount >= 1, 100),
        Badge("team_player", "Team Player", "Set up 3 or more categories", "🤝", Color(0xFF00BCD4), categoryCount >= 3, 100),
        Badge("big_spender", "Big Spender", "Log 25 or more expenses", "💸", Color(0xFFFF5252), totalExpenseCount >= 25, 250),
        Badge("organised", "Organised", "Create 5 or more categories", "🗂️", Color(0xFF009688), categoryCount >= 5, 150)
    )

    val earnedBadges = badges.filter { it.isEarned }
    val lockedBadges = badges.filter { !it.isEarned }

    val baseActionXp = (totalExpenseCount * 10) + (categoryCount * 20) + (goalCount * 30)
    val earnedBadgeXp = earnedBadges.sumOf { it.xpReward }
    val totalXpEarned = baseActionXp + earnedBadgeXp

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

    Column(modifier = Modifier.fillMaxSize().background(screenBackground()).verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 20.dp, top = 40.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = contentPrimary()) }
            Text("My Achievements", color = contentPrimary(), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = if (isDark) NavyMedium else Color.White)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFFFD700)), contentAlignment = Alignment.Center) { Text("$currentLevel", color = NavyDark, fontWeight = FontWeight.Bold, fontSize = 22.sp) }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Level $currentLevel — $levelTitle", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("$xpIntoLevel / $xpPerLevel XP to next level", color = contentSecondary(), fontSize = 12.sp)
                    }
                    Text("$totalXpEarned XP", color = LimeGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(if (isDark) NavyLight else Color(0xFFF0F4FF))) {
                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(xpProgress.coerceIn(0f, 1f)).clip(RoundedCornerShape(5.dp)).background(Color(0xFFFFD700)))
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        if (earnedBadges.isNotEmpty()) {
            Text("EARNED BADGES", color = contentSecondary(), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
            earnedBadges.chunked(3).forEach { rowBadges ->
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowBadges.forEach { badge -> BadgeCard(badge, Modifier.weight(1f)) }
                    repeat(3 - rowBadges.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        if (lockedBadges.isNotEmpty()) {
            Text("LOCKED BADGES", color = contentSecondary(), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
            lockedBadges.chunked(3).forEach { rowBadges ->
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowBadges.forEach { badge -> BadgeCard(badge, Modifier.weight(1f)) }
                    repeat(3 - rowBadges.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun BadgeCard(badge: Badge, modifier: Modifier = Modifier) {
    val isDark = LocalDarkMode.current
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = if (badge.isEarned) { if (isDark) NavyMedium else Color.White } else { if (isDark) NavyLight.copy(alpha = 0.4f) else Color(0xFFE5E9F2) })) {
        Column(modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(if (badge.isEarned) badge.iconColor else badge.iconColor.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
                Text(badge.icon, color = if (badge.isEarned) Color.White else Color.White.copy(alpha = 0.5f), fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
            Text(badge.title, color = if (badge.isEarned) contentPrimary() else contentSecondary(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, maxLines = 2)
            Text(badge.description, color = contentSecondary(), fontSize = 8.sp, lineHeight = 10.sp, textAlign = TextAlign.Center, maxLines = 3)
            if (badge.isEarned) {
                Text("+${badge.xpReward} XP", color = LimeGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
        }
    }
}