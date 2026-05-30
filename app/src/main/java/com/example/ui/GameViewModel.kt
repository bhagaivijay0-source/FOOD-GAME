package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.game.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class AppScreen {
    MENU,
    LEVEL_SELECTOR,
    GAME,
    ACHIEVEMENTS,
    STORE,
    DAILY_REWARDS
}

enum class LevelLimitType {
    MOVES,
    TIME
}

data class LevelDetails(
    val id: Int,
    val name: String,
    val targetScore: Int,
    val limitType: LevelLimitType,
    val limitValue: Int, // Moves or seconds
    val allowedFoods: List<FoodType>
)

data class SplashEffect(
    val message: String,
    val scoreGained: Int
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GameRepository(application)
    private val solver = Match3Solver(8, 8)

    // Persistent flows
    val userStats: StateFlow<UserStats?> = repository.userStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val levels: StateFlow<List<LevelScore>> = repository.allLevels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<GameAchievement>> = repository.allAchievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active screen state
    private val _currentScreen = MutableStateFlow(AppScreen.MENU)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Level static definitions
    val levelManifest = listOf(
        LevelDetails(1, "Samosa Corner", 1000, LevelLimitType.MOVES, 25, listOf(FoodType.SAMOSA, FoodType.IDLI, FoodType.DOSA)),
        LevelDetails(2, "Idli Steamer", 1500, LevelLimitType.MOVES, 25, listOf(FoodType.SAMOSA, FoodType.IDLI, FoodType.DOSA, FoodType.VADA)),
        LevelDetails(3, "Dosa Dhaba", 1500, LevelLimitType.TIME, 45, listOf(FoodType.SAMOSA, FoodType.IDLI, FoodType.DOSA, FoodType.VADA)),
        LevelDetails(4, "Vada Junction", 2500, LevelLimitType.MOVES, 28, listOf(FoodType.SAMOSA, FoodType.IDLI, FoodType.DOSA, FoodType.VADA, FoodType.JALEBI)),
        LevelDetails(5, "Jalebi Street", 2500, LevelLimitType.TIME, 60, listOf(FoodType.SAMOSA, FoodType.IDLI, FoodType.VADA, FoodType.JALEBI, FoodType.GULAB_JAMUN)),
        LevelDetails(6, "Gulab Jamun Feast", 4000, LevelLimitType.MOVES, 25, listOf(FoodType.SAMOSA, FoodType.IDLI, FoodType.JALEBI, FoodType.GULAB_JAMUN, FoodType.PANI_PURI)),
        LevelDetails(7, "Pani Puri Splash", 4500, LevelLimitType.MOVES, 30, listOf(FoodType.SAMOSA, FoodType.JALEBI, FoodType.GULAB_JAMUN, FoodType.PANI_PURI, FoodType.BIRYANI)),
        LevelDetails(8, "Biryani Darbar", 6000, LevelLimitType.TIME, 90, listOf(FoodType.SAMOSA, FoodType.JALEBI, FoodType.GULAB_JAMUN, FoodType.PANI_PURI, FoodType.BIRYANI)),
        LevelDetails(9, "Grand Food Mela", 8000, LevelLimitType.MOVES, 32, listOf(FoodType.SAMOSA, FoodType.IDLI, FoodType.DOSA, FoodType.VADA, FoodType.JALEBI, FoodType.GULAB_JAMUN, FoodType.PANI_PURI)),
        LevelDetails(10, "Maha Festival Feast", 12000, LevelLimitType.MOVES, 22, FoodType.values().toList())
    )

    // Active game states
    private val _activeLevel = MutableStateFlow<LevelDetails?>(null)
    val activeLevel: StateFlow<LevelDetails?> = _activeLevel.asStateFlow()

    private val _board = MutableStateFlow<Array<Array<Tile>>?>(null)
    val board: StateFlow<Array<Array<Tile>>?> = _board.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _movesLeft = MutableStateFlow(0)
    val movesLeft: StateFlow<Int> = _movesLeft.asStateFlow()

    private val _timeLeft = MutableStateFlow(0)
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    private val _comboMultiplier = MutableStateFlow(1)
    val comboMultiplier: StateFlow<Int> = _comboMultiplier.asStateFlow()

    private val _selectedTile = MutableStateFlow<Pair<Int, Int>?>(null)
    val selectedTile: StateFlow<Pair<Int, Int>?> = _selectedTile.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // Game Flow States (Win / No Moves dialog triggers)
    private val _showWinDialog = MutableStateFlow(false)
    val showWinDialog: StateFlow<Boolean> = _showWinDialog.asStateFlow()

    private val _showFailDialog = MutableStateFlow(false)
    val showFailDialog: StateFlow<Boolean> = _showFailDialog.asStateFlow()

    // Visual Splash indicators ("Spicy!", "Yum!", "Shahi Combo!")
    private val _splashEffect = MutableStateFlow<SplashEffect?>(null)
    val splashEffect: StateFlow<SplashEffect?> = _splashEffect.asStateFlow()

    // Daily Reward visual flow
    private val _dailyRewardMessage = MutableStateFlow<String?>(null)
    val dailyRewardMessage: StateFlow<String?> = _dailyRewardMessage.asStateFlow()

    // Ads reward flow
    private val _adProgressSeconds = MutableStateFlow<Int?>(null)
    val adProgressSeconds: StateFlow<Int?> = _adProgressSeconds.asStateFlow()

    private var countdownTimerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initializeDataIfNeeded()
        }
    }

    fun navigateTo(screen: AppScreen) {
        // Stop timer if moving away from board
        if (screen != AppScreen.GAME) {
            countdownTimerJob?.cancel()
        }
        _currentScreen.value = screen
    }

    fun startLevel(levelId: Int) {
        val details = levelManifest.find { it.id == levelId } ?: return
        _activeLevel.value = details
        _score.value = 0
        _selectedTile.value = null
        _showWinDialog.value = false
        _showFailDialog.value = false
        _isProcessing.value = false
        _splashEffect.value = null
        _comboMultiplier.value = 1

        val initialBoard = solver.generateInitialBoard(details.allowedFoods)
        _board.value = initialBoard

        when (details.limitType) {
            LevelLimitType.MOVES -> {
                _movesLeft.value = details.limitValue
                _timeLeft.value = 0
            }
            LevelLimitType.TIME -> {
                _movesLeft.value = 0
                _timeLeft.value = details.limitValue
                startCountdownTimer()
            }
        }
        _currentScreen.value = AppScreen.GAME
    }

    private fun startCountdownTimer() {
        countdownTimerJob?.cancel()
        countdownTimerJob = viewModelScope.launch {
            while (_timeLeft.value > 0) {
                delay(1000)
                if (!_isProcessing.value && !_showWinDialog.value && !_showFailDialog.value) {
                    _timeLeft.value -= 1
                    if (_timeLeft.value == 0) {
                        evaluateEndGame()
                    }
                }
            }
        }
    }

    fun onTileSelected(r: Int, c: Int) {
        if (_isProcessing.value || _showWinDialog.value || _showFailDialog.value) return

        val currentSelected = _selectedTile.value
        if (currentSelected == null) {
            _selectedTile.value = Pair(r, c)
        } else {
            // Check if they tapped the same tile again - deselect
            if (currentSelected == Pair(r, c)) {
                _selectedTile.value = null
                return
            }

            // Check if adjacent
            if (solver.isAdjacent(currentSelected, Pair(r, c))) {
                _selectedTile.value = null
                triggerSwap(currentSelected, Pair(r, c))
            } else {
                // Not adjacent, set the new tile as selected
                _selectedTile.value = Pair(r, c)
            }
        }
    }

    private fun triggerSwap(p1: Pair<Int, Int>, p2: Pair<Int, Int>) {
        val currentBoard = _board.value ?: return
        _isProcessing.value = true

        viewModelScope.launch {
            // 1. Temporarily swap titles in state
            val swappedBoard = Array(solver.rows) { r ->
                Array(solver.cols) { c ->
                    val origin = currentBoard[r][c]
                    when {
                        r == p1.first && c == p1.second -> currentBoard[p2.first][p2.second].copy(row = p1.first, col = p1.second)
                        r == p2.first && c == p2.second -> currentBoard[p1.first][p1.second].copy(row = p2.first, col = p2.second)
                        else -> origin
                    }
                }
            }
            _board.value = swappedBoard
            delay(150) // swap animation speed

            // 2. Solve matches
            val level = _activeLevel.value ?: return@launch
            val matchRes = solver.findAndMarkMatches(swappedBoard, p1, p2)

            // Let's also check if they swapped a Biryani Feast Color Bomb with ANY standard tile!
            // In Candy Crush, the Color Bomb clears all tiles matching the swapped standard item.
            var isColorBombInteraction = false
            val t1 = swappedBoard[p1.first][p1.second]
            val t2 = swappedBoard[p2.first][p2.second]
            
            val finalMatchRes = if (t1.specialType == SpecialType.BIRYANI_FEAST || t2.specialType == SpecialType.BIRYANI_FEAST) {
                isColorBombInteraction = true
                val bombCoord = if (t1.specialType == SpecialType.BIRYANI_FEAST) p1 else p2
                val foodTargetCoord = if (t1.specialType == SpecialType.BIRYANI_FEAST) p2 else p1
                val targetFoodType = swappedBoard[foodTargetCoord.first][foodTargetCoord.second].foodType
                
                // Explode the bomb and all instances of targetFoodType
                val exploded = mutableSetOf(bombCoord)
                for (r in 0 until solver.rows) {
                    for (c in 0 until solver.cols) {
                        if (swappedBoard[r][c].foodType == targetFoodType) {
                            exploded.add(Pair(r, c))
                        }
                    }
                }
                MatchResult(exploded, mapOf())
            } else {
                matchRes
            }

            if (finalMatchRes.matchedCoords.isNotEmpty() || isColorBombInteraction) {
                // Decrease move count!
                if (level.limitType == LevelLimitType.MOVES) {
                    _movesLeft.value -= 1
                }

                _comboMultiplier.value = 1
                executeExplosionAndCascade(swappedBoard, finalMatchRes.matchedCoords, finalMatchRes.specialsToCreate)
            } else {
                // Invalid swap! Swap back
                val revertedBoard = Array(solver.rows) { r ->
                    Array(solver.cols) { c ->
                        val origin = swappedBoard[r][c]
                        when {
                            r == p1.first && c == p1.second -> swappedBoard[p2.first][p2.second].copy(row = p1.first, col = p1.second)
                            r == p2.first && c == p2.second -> swappedBoard[p1.first][p1.second].copy(row = p2.first, col = p2.second)
                            else -> origin
                        }
                    }
                }
                _board.value = revertedBoard
                _isProcessing.value = false
            }
        }
    }

    private suspend fun executeExplosionAndCascade(
        currentBoard: Array<Array<Tile>>,
        matchedCoords: Set<Pair<Int, Int>>,
        specialsToCreate: Map<Pair<Int, Int>, SpecialType>
    ) {
        val level = _activeLevel.value ?: return
        val resolution = solver.resolveExplosionsAndSpecials(currentBoard, matchedCoords, specialsToCreate, level.allowedFoods)
        
        // 1. Show marked exploding board
        _board.value = resolution.board
        
        // Award points based on tiles popped!
        var pointsGained = resolution.fullyExplodedCoords.size * 60 * _comboMultiplier.value
        if (specialsToCreate.isNotEmpty()) {
            pointsGained += specialsToCreate.size * 200 // Bonus for making special recipes!
        }
        _score.value += pointsGained

        // Achievements progression for special items built
        resolution.fullyExplodedCoords.forEach { coord ->
            val tile = currentBoard[coord.first][coord.second]
            if (tile.foodType == FoodType.SAMOSA) {
                repository.incrementAchievementProgress("samosa_basher", 1)
            }
        }
        specialsToCreate.values.forEach { special ->
            when (special) {
                SpecialType.SPICY_SAMOSA_BOMB -> repository.incrementAchievementProgress("samosa_bomber", 1)
                SpecialType.JALEBI_SWIRL_ROW -> repository.incrementAchievementProgress("jalebi_maker", 1)
                SpecialType.PANI_PURI_SPLASH_COL -> repository.incrementAchievementProgress("pani_puri_slasher", 1)
                SpecialType.BIRYANI_FEAST -> repository.incrementAchievementProgress("biryani_feaster", 1)
                else -> {}
            }
        }

        // Show combos text overlays
        triggerComboSplash(pointsGained)

        delay(350) // let animations pop

        // 2. Perform falling-fill
        val gravityRes = solver.applyGravityAndRefill(resolution.board, level.allowedFoods)
        _board.value = gravityRes.board

        delay(300) // gravity falling delay

        // 3. Check for cascade combos
        val postCascadeMatches = solver.findAndMarkMatches(gravityRes.board)
        if (postCascadeMatches.matchedCoords.isNotEmpty()) {
            _comboMultiplier.value += 1
            repository.incrementAchievementProgress("cascade_pro", _comboMultiplier.value)
            
            // Loop cascades
            executeExplosionAndCascade(
                gravityRes.board,
                postCascadeMatches.matchedCoords,
                postCascadeMatches.specialsToCreate
            )
        } else {
            // No more cascades. Turn ends!
            _isProcessing.value = false
            evaluateEndGame()
        }
    }

    private fun triggerComboSplash(points: Int) {
        val words = listOf("Yum!", "Delicious!", "Crispy Samosa!", "Jalebi Swirl!", "Spicy Delight!", "Shahi Festival!", "Chaotic Feast!")
        val randomWord = when (_comboMultiplier.value) {
            1 -> if (points > 400) words[0] else null
            2 -> words[1]
            3 -> words[2]
            4 -> words[4]
            else -> words[5]
        }
        if (randomWord != null) {
            _splashEffect.value = SplashEffect(randomWord, points)
            viewModelScope.launch {
                delay(1200)
                _splashEffect.value = null
            }
        }
    }

    private fun evaluateEndGame() {
        val level = _activeLevel.value ?: return
        val currentScore = _score.value
        val matchesRemaining = solver.hasMatches(_board.value ?: emptyArray())

        viewModelScope.launch {
            if (currentScore >= level.targetScore) {
                // WIN! Calculate stars
                val ratio = currentScore.toFloat() / level.targetScore
                val stars = when {
                    ratio >= 2.0f -> 3
                    ratio >= 1.4f -> 2
                    else -> 1
                }
                
                // Reward coins based on star rating!
                val reward = stars * 35
                repository.addCoins(reward)
                repository.saveLevelScores(level.id, currentScore, stars)

                _showWinDialog.value = true
            } else {
                // Check if strictly out of resources
                val isOutOfMoves = level.limitType == LevelLimitType.MOVES && _movesLeft.value <= 0
                val isOutOfTime = level.limitType == LevelLimitType.TIME && _timeLeft.value <= 0

                if (isOutOfMoves || isOutOfTime) {
                    _showFailDialog.value = true
                }
            }
        }
    }

    // Spend 50 coins to get 5 extra moves (or +30 extra seconds for timed levels)
    fun purchaseExtraMoves() {
        viewModelScope.launch {
            val stats = repository.getLiveStats()
            if (stats.coins >= 50) {
                repository.addCoins(-50)
                _showFailDialog.value = false
                val level = _activeLevel.value ?: return@launch
                if (level.limitType == LevelLimitType.MOVES) {
                    _movesLeft.value += 5
                } else {
                    _timeLeft.value += 30
                    startCountdownTimer()
                }
            }
        }
    }

    // Simulated Ad: player watches short 3s ad overlay to earn bonus moves without taking coins!
    fun watchAdForExtraMoves() {
        _isProcessing.value = true
        _showFailDialog.value = false
        viewModelScope.launch {
            for (i in 3 downTo 1) {
                _adProgressSeconds.value = i
                delay(1000)
            }
            _adProgressSeconds.value = null
            _isProcessing.value = false
            
            // Reward 5 moves
            val level = _activeLevel.value ?: return@launch
            if (level.limitType == LevelLimitType.MOVES) {
                _movesLeft.value += 5
            } else {
                _timeLeft.value += 30
                startCountdownTimer()
            }
        }
    }

    fun purchaseTheme(themeName: String, cost: Int) {
        viewModelScope.launch {
            repository.purchaseTheme(themeName, cost)
        }
    }

    fun activateTheme(themeName: String) {
        viewModelScope.launch {
            repository.setThemeActive(themeName)
        }
    }

    fun togglePremium() {
        viewModelScope.launch {
            val stats = repository.getLiveStats()
            repository.setPremiumStatus(!stats.adFree)
        }
    }

    fun claimDailyReward() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val stats = repository.getLiveStats()
            // Check if 24 hours (86400000 ms) passed
            val diff = now - stats.lastDailyRewardClaimed
            if (diff >= 86400000 || stats.lastDailyRewardClaimed == 0L) {
                val coins = Random.nextInt(50, 151) // earn 50 to 150 coins!
                repository.updateDailyRewardClaimed(now, coins)
                _dailyRewardMessage.value = "Namaste! You claimed today's festival sweet box and found 🪙 $coins Coins!"
                delay(4000)
                _dailyRewardMessage.value = null
            } else {
                _dailyRewardMessage.value = "The Halwai is preparing tomorrow's sweets! Check back in details."
                delay(3000)
                _dailyRewardMessage.value = null
            }
        }
    }
}
