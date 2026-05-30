package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. Entities
@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 100,
    val adFree: Boolean = false,
    val unlockedThemes: String = "Classic", // Comma-separated list
    val activeTheme: String = "Classic",
    val lastDailyRewardClaimed: Long = 0,
    val levelReached: Int = 1
)

@Entity(tableName = "level_score")
data class LevelScore(
    @PrimaryKey val levelId: Int,
    val highScore: Int = 0,
    val stars: Int = 0,
    val isUnlocked: Boolean = false
)

@Entity(tableName = "achievements")
data class GameAchievement(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val progress: Int = 0,
    val target: Int,
    val isUnlocked: Boolean = false
)

// 2. DAOs
@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
    fun getUserStatsFlow(): Flow<UserStats?>

    @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
    suspend fun getUserStats(): UserStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: UserStats)
}

@Dao
interface LevelScoreDao {
    @Query("SELECT * FROM level_score ORDER BY levelId ASC")
    fun getAllLevelsFlow(): Flow<List<LevelScore>>

    @Query("SELECT * FROM level_score WHERE levelId = :levelId LIMIT 1")
    suspend fun getLevel(levelId: Int): LevelScore?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(level: LevelScore)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(levels: List<LevelScore>)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY id ASC")
    fun getAllAchievementsFlow(): Flow<List<GameAchievement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(achievement: GameAchievement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<GameAchievement>)
}

// 3. Database
@Database(
    entities = [UserStats::class, LevelScore::class, GameAchievement::class],
    version = 1,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {
    abstract val userStatsDao: UserStatsDao
    abstract val levelScoreDao: LevelScoreDao
    abstract val achievementDao: AchievementDao
}
