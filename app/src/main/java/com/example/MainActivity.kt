package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.*

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: GameViewModel = viewModel()
        val currentScreen by viewModel.currentScreen.collectAsState()
        val userStats by viewModel.userStats.collectAsState()
        val levels by viewModel.levels.collectAsState()
        val achievements by viewModel.achievements.collectAsState()
        val dailyRewardMessage by viewModel.dailyRewardMessage.collectAsState()

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          BoxWithModifier(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
              AppScreen.MENU -> {
                MenuScreen(
                  userStats = userStats,
                  onNavigate = { screen -> viewModel.navigateTo(screen) },
                  onClaimDaily = { viewModel.claimDailyReward() }
                )
              }
              AppScreen.LEVEL_SELECTOR -> {
                LevelSelectorScreen(
                  levels = levels,
                  userStats = userStats,
                  levelManifest = viewModel.levelManifest,
                  onBack = { viewModel.navigateTo(AppScreen.MENU) },
                  onLevelSelected = { levelId -> viewModel.startLevel(levelId) }
                )
              }
              AppScreen.ACHIEVEMENTS -> {
                AchievementsScreen(
                  achievements = achievements,
                  onBack = { viewModel.navigateTo(AppScreen.MENU) }
                )
              }
              AppScreen.STORE -> {
                StoreScreen(
                  userStats = userStats,
                  onBack = { viewModel.navigateTo(AppScreen.MENU) },
                  onPurchaseTheme = { theme, cost -> viewModel.purchaseTheme(theme, cost) },
                  onActivateTheme = { theme -> viewModel.activateTheme(theme) },
                  onTogglePremium = { viewModel.togglePremium() }
                )
              }
              AppScreen.DAILY_REWARDS -> {
                DailyRewardsScreen(
                  userStats = userStats,
                  message = dailyRewardMessage,
                  onBack = { viewModel.navigateTo(AppScreen.MENU) },
                  onClaim = { viewModel.claimDailyReward() }
                )
              }
              AppScreen.GAME -> {
                GamePlayScreen(
                  viewModel = viewModel,
                  userStats = userStats,
                  onNavigate = { screen -> viewModel.navigateTo(screen) }
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun BoxWithModifier(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
  content()
}
