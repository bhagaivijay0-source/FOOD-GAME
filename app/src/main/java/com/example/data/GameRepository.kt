package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class GameRepository(context: Context) {
    private val database: GameDatabase = Room.databaseBuilder(
        context.applicationContext,
        GameDatabase::class.java,
        "street_food_smash_db"
    ).build()

    private val userStatsDao = database.userStatsDao
    private val levelScoreDao = database.levelScoreDao
    private val achievementDao = database.achievementDao

    val userStats: Flow<UserStats?> = userStatsDao.getUserStatsFlow()
    val allLevels: Flow<List<LevelScore>> = levelScoreDao.getAllLevelsFlow()
    val allAchievements: Flow<List<GameAchievement>> = achievementDao.getAllAchievementsFlow()

    suspend fun getLiveStats(): UserStats {
        return withContext(Dispatchers.IO) {
            userStatsDao.getUserStats() ?: UserStats().also {
                userStatsDao.insertOrUpdate(it)
            }
        }
    }

    suspend fun initializeDataIfNeeded() {
        withContext(Dispatchers.IO) {
            // 1. Seed user stats
            val stats = userStatsDao.getUserStats()
            if (stats == null) {
                userStatsDao.insertOrUpdate(UserStats())
            }

            // 2. Seed Level details if empty
            val levels = levelScoreDao.getAllLevelsFlow().firstOrNull() ?: emptyList()
            if (levels.isEmpty()) {
                val seedLevels = listOf(
                    LevelScore(levelId = 1, highScore = 0, stars = 0, isUnlocked = true),
                    LevelScore(levelId = 2, highScore = 0, stars = 0, isUnlocked = false),
                    LevelScore(levelId = 3, highScore = 0, stars = 0, isUnlocked = false),
                    LevelScore(levelId = 4, highScore = 0, stars = 0, isUnlocked = false),
                    LevelScore(levelId = 5, highScore = 0, stars = 0, isUnlocked = false),
                    LevelScore(levelId = 6, highScore = 0, stars = 0, isUnlocked = false),
                    LevelScore(levelId = 7, highScore = 0, stars = 0, isUnlocked = false),
                    LevelScore(levelId = 8, highScore = 0, stars = 0, isUnlocked = false),
                    LevelScore(levelId = 9, highScore = 0, stars = 0, isUnlocked = false),
                    LevelScore(levelId = 10, highScore = 0, stars = 0, isUnlocked = false)
                )
                levelScoreDao.insertAll(seedLevels)
            }

            // 3. Seed Achievements if empty
            val achievements = achievementDao.getAllAchievementsFlow().firstOrNull() ?: emptyList()
            if (achievements.isEmpty()) {
                val seedAchievements = listOf(
                    GameAchievement(
                        id = "samosa_basher",
                        title = "Samosa Muncher",
                        description = "Match 100 Samosas across all levels",
                        target = 100
                    ),
                    GameAchievement(
                        id = "jalebi_maker",
                        title = "Jalebi Halwai",
                        description = "Create 15 Jalebi Swirl special rows",
                        target = 15
                    ),
                    GameAchievement(
                        id = "pani_puri_slasher",
                        title = "Pani Puri Chef",
                        description = "Create 15 Pani Puri Splash special columns",
                        target = 15
                    ),
                    GameAchievement(
                        id = "samosa_bomber",
                        title = "Chilli Firebomber",
                        description = "Create 10 Spicy Samosa Bombs",
                        target = 10
                    ),
                    GameAchievement(
                        id = "biryani_feaster",
                        title = "Biryani Shahi Feast",
                        description = "Match 5 Biryani Bowls to trigger a global feast",
                        target = 5
                    ),
                    GameAchievement(
                        id = "cascade_pro",
                        title = "Combo Dhaba King",
                        description = "Achieve a combo chain reaction of 5x or more",
                        target = 5
                    ),
                    GameAchievement(
                        id = "coin_collector",
                        title = "Wealthy Foodie",
                        description = "Earn 500 total coins in the festival store",
                        target = 500
                    ),
                    GameAchievement(
                        id = "festival_champion",
                        title = "Festival Guru",
                        description = "Reach Level 10 of the Indian Street Food Mela",
                        target = 10
                    )
                )
                achievementDao.insertAll(seedAchievements)
            }
        }
    }

    suspend fun saveLevelScores(levelId: Int, score: Int, stars: Int) {
        withContext(Dispatchers.IO) {
            val existing = levelScoreDao.getLevel(levelId)
            val updatedHighScore = if (existing != null && score > existing.highScore) score else (existing?.highScore ?: score)
            val updatedStars = if (existing != null && stars > existing.stars) stars else (existing?.stars ?: stars)

            levelScoreDao.insertOrUpdate(
                LevelScore(
                    levelId = levelId,
                    highScore = updatedHighScore,
                    stars = updatedStars,
                    isUnlocked = true
                )
            )

            // Unlock next level
            val nextLevelId = levelId + 1
            if (nextLevelId <= 10) {
                val nextExisting = levelScoreDao.getLevel(nextLevelId)
                if (nextExisting == null || !nextExisting.isUnlocked) {
                    levelScoreDao.insertOrUpdate(
                        LevelScore(
                            levelId = nextLevelId,
                            highScore = nextExisting?.highScore ?: 0,
                            stars = nextExisting?.stars ?: 0,
                            isUnlocked = true
                        )
                    )
                }
            }

            // Update Level-related achievement progress
            val stats = getLiveStats()
            val finalLevelReached = if (nextLevelId > stats.levelReached) {
                if (nextLevelId <= 10) nextLevelId else stats.levelReached
            } else {
                stats.levelReached
            }
            userStatsDao.insertOrUpdate(stats.copy(levelReached = finalLevelReached))

            updateAchievementProgress("festival_champion", finalLevelReached)
        }
    }

    suspend fun addCoins(amount: Int) {
        withContext(Dispatchers.IO) {
            val stats = getLiveStats()
            val newCoins = stats.coins + amount
            userStatsDao.insertOrUpdate(stats.copy(coins = newCoins))
            updateAchievementProgress("coin_collector", newCoins)
        }
    }

    suspend fun purchaseTheme(themeName: String, cost: Int): Boolean {
        return withContext(Dispatchers.IO) {
            val stats = getLiveStats()
            if (stats.coins >= cost) {
                val list = stats.unlockedThemes.split(",").toMutableList()
                if (!list.contains(themeName)) {
                    list.add(themeName)
                }
                userStatsDao.insertOrUpdate(
                    stats.copy(
                        coins = stats.coins - cost,
                        unlockedThemes = list.joinToString(","),
                        activeTheme = themeName
                    )
                )
                true
            } else {
                false
            }
        }
    }

    suspend fun setThemeActive(themeName: String) {
        withContext(Dispatchers.IO) {
            val stats = getLiveStats()
            userStatsDao.insertOrUpdate(stats.copy(activeTheme = themeName))
        }
    }

    suspend fun updateDailyRewardClaimed(claimTime: Long, coinsEarned: Int) {
        withContext(Dispatchers.IO) {
            val stats = getLiveStats()
            userStatsDao.insertOrUpdate(
                stats.copy(
                    lastDailyRewardClaimed = claimTime,
                    coins = stats.coins + coinsEarned
                )
            )
            updateAchievementProgress("coin_collector", stats.coins + coinsEarned)
        }
    }

    suspend fun setPremiumStatus(isPremium: Boolean) {
        withContext(Dispatchers.IO) {
            val stats = getLiveStats()
            userStatsDao.insertOrUpdate(stats.copy(adFree = isPremium))
        }
    }

    suspend fun incrementAchievementProgress(id: String, amount: Int = 1) {
        withContext(Dispatchers.IO) {
            // Find achievement
            val list = allAchievements.firstOrNull() ?: emptyList()
            val match = list.find { it.id == id }
            if (match != null) {
                val newProgress = (match.progress + amount).coerceAtMost(match.target)
                val unlocked = newProgress >= match.target
                achievementDao.insertOrUpdate(
                    match.copy(
                        progress = newProgress,
                        isUnlocked = unlocked
                    )
                )
            }
        }
    }

    private suspend fun updateAchievementProgress(id: String, absoluteValue: Int) {
        val list = allAchievements.firstOrNull() ?: emptyList()
        val match = list.find { it.id == id }
        if (match != null) {
            val newProgress = absoluteValue.coerceAtMost(match.target)
            val unlocked = newProgress >= match.target
            achievementDao.insertOrUpdate(
                match.copy(
                    progress = newProgress,
                    isUnlocked = unlocked
                )
            )
        }
    }
}
