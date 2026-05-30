package com.example.game

import kotlin.math.abs
import kotlin.random.Random

enum class FoodType {
    SAMOSA,      // Yellow triangle
    IDLI,        // Fluffy white disc
    DOSA,        // Rolled golden crepe
    VADA,        // Golden-brown ring donut
    JALEBI,      // Orange spiral swirl
    GULAB_JAMUN, // Sweet brown sphere
    PANI_PURI,   // Hollow gold sphere with green splash
    BIRYANI      // Saffron clay pot
}

enum class SpecialType {
    NONE,
    SPICY_SAMOSA_BOMB,    // Clears 3x3 surrounding
    JALEBI_SWIRL_ROW,     // Clears row
    PANI_PURI_SPLASH_COL, // Clears column
    BIRYANI_FEAST         // Color bomb (clears all of one type)
}

data class Tile(
    val id: String,
    val row: Int,
    val col: Int,
    val foodType: FoodType,
    val specialType: SpecialType = SpecialType.NONE,
    val isExploding: Boolean = false,
    val isMatched: Boolean = false,
    // Animation offsets for dropping animation
    val startYOffset: Float = 0f, 
    val currentYOffset: Float = 0f
)

class Match3Solver(val rows: Int = 8, val cols: Int = 8) {

    fun generateInitialBoard(allowedTypes: List<FoodType>): Array<Array<Tile>> {
        var board = Array(rows) { r ->
            Array(cols) { c ->
                createRandomTile(r, c, allowedTypes)
            }
        }
        
        // Ensure there are no pre-existing match-3 on initial board
        var attempts = 0
        while (hasMatches(board) && attempts < 100) {
            board = Array(rows) { r ->
                Array(cols) { c ->
                    createRandomTile(r, c, allowedTypes)
                }
            }
            attempts++
        }
        return board
    }

    private fun createRandomTile(row: Int, col: Int, allowedTypes: List<FoodType>): Tile {
        val type = allowedTypes[Random.nextInt(allowedTypes.size)]
        return Tile(
            id = "tile_${row}_${col}_${System.nanoTime()}_${Random.nextInt(1000)}",
            row = row,
            col = col,
            foodType = type
        )
    }

    // Checks if the tile grid currently contains any match of 3 or more
    fun hasMatches(board: Array<Array<Tile>>): Boolean {
        // Horizontal check
        for (r in 0 until rows) {
            for (c in 0 until cols - 2) {
                val t0 = board[r][c]
                val t1 = board[r][c+1]
                val t2 = board[r][c+2]
                if (t0.foodType == t1.foodType && t1.foodType == t2.foodType) {
                    return true
                }
            }
        }
        // Vertical check
        for (r in 0 until rows - 2) {
            for (c in 0 until cols) {
                val t0 = board[r][c]
                val t1 = board[r+1][c]
                val t2 = board[r+2][c]
                if (t0.foodType == t1.foodType && t1.foodType == t2.foodType) {
                    return true
                }
            }
        }
        return false
    }

    // Returns a list of coordinates to explode and specifies any special tiles created
    fun findAndMarkMatches(
        board: Array<Array<Tile>>,
        swapFrom: Pair<Int, Int>? = null,
        swapTo: Pair<Int, Int>? = null
    ): MatchResult {
        val matchedCoords = mutableSetOf<Pair<Int, Int>>()
        val specialsToCreate = mutableMapOf<Pair<Int, Int>, SpecialType>()
        val matchAccumulator = Array(rows) { BooleanArray(cols) }

        // Find Horizontal matches of 3, 4, or 5
        for (r in 0 until rows) {
            var c = 0
            while (c < cols - 2) {
                val type = board[r][c].foodType
                var matchLen = 1
                while (c + matchLen < cols && board[r][c + matchLen].foodType == type) {
                    matchLen++
                }

                if (matchLen >= 3) {
                    for (i in 0 until matchLen) {
                        matchAccumulator[r][c + i] = true
                    }

                    // Special creations:
                    // Match of 5: color bomb (Biryani Feast)
                    // Match of 4: row clearer (Jalebi Swirl Row)
                    val specialTarget = getPreferredSpecialPlacement(r, c, matchLen, horizontal = true, swapFrom, swapTo)
                    if (matchLen >= 5) {
                        specialsToCreate[specialTarget] = SpecialType.BIRYANI_FEAST
                    } else if (matchLen == 4) {
                        specialsToCreate[specialTarget] = SpecialType.JALEBI_SWIRL_ROW
                    }
                    c += matchLen
                } else {
                    c++
                }
            }
        }

        // Find Vertical matches of 3, 4, or 5
        for (c in 0 until cols) {
            var r = 0
            while (r < rows - 2) {
                val type = board[r][c].foodType
                var matchLen = 1
                while (r + matchLen < rows && board[r + matchLen][c].foodType == type) {
                    matchLen++
                }

                if (matchLen >= 3) {
                    for (i in 0 until matchLen) {
                        matchAccumulator[r + i][c] = true
                    }

                    // Special creations:
                    // Match of 5: color bomb (Biryani Feast)
                    // Match of 4: col clearer (Pani Puri Splash Col)
                    val specialTarget = getPreferredSpecialPlacement(r, c, matchLen, horizontal = false, swapFrom, swapTo)
                    if (matchLen >= 5) {
                        specialsToCreate[specialTarget] = SpecialType.BIRYANI_FEAST
                    } else if (matchLen == 4) {
                        specialsToCreate[specialTarget] = SpecialType.PANI_PURI_SPLASH_COL
                    }
                    r += matchLen
                } else {
                    r++
                }
            }
        }

        // Detect T-shapes and L-shapes for Samosa Bomb
        // A coordinate is in an intersection if it stands in BOTH horizontal and vertical matches.
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (matchAccumulator[r][c]) {
                    matchedCoords.add(Pair(r, c))
                    
                    // Check if it's an intersection!
                    val isHorizMatch = hasHorizontalMatchAround(board, r, c)
                    val isVertMatch = hasVerticalMatchAround(board, r, c)
                    if (isHorizMatch && isVertMatch) {
                        // Mark as Samosa fire bomb (L/T Shape reward!)
                        specialsToCreate[Pair(r, c)] = SpecialType.SPICY_SAMOSA_BOMB
                    }
                }
            }
        }

        return MatchResult(matchedCoords, specialsToCreate)
    }

    private fun hasHorizontalMatchAround(board: Array<Array<Tile>>, r: Int, c: Int): Boolean {
        var len = 1
        val type = board[r][c].foodType
        // check left
        var cl = c - 1
        while (cl >= 0 && board[r][cl].foodType == type) { cl--; len++ }
        // check right
        var cr = c + 1
        while (cr < cols && board[r][cr].foodType == type) { cr++; len++ }
        return len >= 3
    }

    private fun hasVerticalMatchAround(board: Array<Array<Tile>>, r: Int, c: Int): Boolean {
        var len = 1
        val type = board[r][c].foodType
        // check up
        var ru = r - 1
        while (ru >= 0 && board[ru][c].foodType == type) { ru--; len++ }
        // check down
        var rd = r + 1
        while (rd < rows && board[rd][c].foodType == type) { rd++; len++ }
        return len >= 3
    }

    // Place special where the user swapped, or centrally
    private fun getPreferredSpecialPlacement(
        start: Int,
        startFixed: Int,
        len: Int,
        horizontal: Boolean,
        swapFrom: Pair<Int, Int>?,
        swapTo: Pair<Int, Int>?
    ): Pair<Int, Int> {
        for (i in 0 until len) {
            val r = if (horizontal) start else start + i
            val c = if (horizontal) startFixed + i else startFixed
            if (Pair(r, c) == swapFrom || Pair(r, c) == swapTo) {
                return Pair(r, c)
            }
        }
        // Default to center of match
        val mid = start + (len / 2)
        return if (horizontal) Pair(mid, startFixed) else Pair(startFixed, mid)
    }

    // Process a full chain activation in case special tiles are cleared
    fun resolveExplosionsAndSpecials(
        board: Array<Array<Tile>>,
        explodingCoords: Set<Pair<Int, Int>>,
        specialsToCreate: Map<Pair<Int, Int>, SpecialType>,
        allFoodTypes: List<FoodType>
    ): ExplosionResolution {
        val finalExplodingCoords = explodingCoords.toMutableSet()
        val queue = explodingCoords.toMutableList()
        val processedSpecials = mutableSetOf<Pair<Int, Int>>()

        // Cascade/resolve special tiles in queue
        while (queue.isNotEmpty()) {
            val curr = queue.removeAt(0)
            val tile = board[curr.first][curr.second]
            
            if (tile.specialType != SpecialType.NONE && curr !in processedSpecials) {
                processedSpecials.add(curr)
                
                when (tile.specialType) {
                    SpecialType.SPICY_SAMOSA_BOMB -> {
                        // explodes surrounding 3x3
                        val rRange = (curr.first - 1).coerceAtLeast(0)..(curr.first + 1).coerceAtMost(rows - 1)
                        val cRange = (curr.second - 1).coerceAtLeast(0)..(curr.second + 1).coerceAtMost(cols - 1)
                        for (r in rRange) {
                            for (c in cRange) {
                                if (Pair(r, c) !in finalExplodingCoords) {
                                    finalExplodingCoords.add(Pair(r, c))
                                    queue.add(Pair(r, c))
                                }
                            }
                        }
                    }
                    SpecialType.JALEBI_SWIRL_ROW -> {
                        // explodes row
                        val r = curr.first
                        for (c in 0 until cols) {
                            if (Pair(r, c) !in finalExplodingCoords) {
                                finalExplodingCoords.add(Pair(r, c))
                                queue.add(Pair(r, c))
                            }
                        }
                    }
                    SpecialType.PANI_PURI_SPLASH_COL -> {
                        // explodes column
                        val c = curr.second
                        for (r in 0 until rows) {
                            if (Pair(r, c) !in finalExplodingCoords) {
                                finalExplodingCoords.add(Pair(r, c))
                                queue.add(Pair(r, c))
                            }
                        }
                    }
                    SpecialType.BIRYANI_FEAST -> {
                        // Clears all tiles of a random type currently popular on the board
                        val typeToClear = determineBiryaniTarget(board, curr, finalExplodingCoords)
                        for (r in 0 until rows) {
                            for (c in 0 until cols) {
                                if (board[r][c].foodType == typeToClear) {
                                    if (Pair(r, c) !in finalExplodingCoords) {
                                        finalExplodingCoords.add(Pair(r, c))
                                        queue.add(Pair(r, c))
                                    }
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }

        // Build the updated board where exploded tiles are marked as matched/isExploding = true,
        // and specialsToCreate are inserted (if they didn't get destroyed immediately)
        val updatedBoard = Array(rows) { r ->
            Array(cols) { c ->
                val tile = board[r][c]
                val coord = Pair(r, c)
                
                if (specialsToCreate.containsKey(coord)) {
                    // Create special tile of this type
                    val special = specialsToCreate[coord]!!
                    Tile(
                        id = "special_${r}_${c}_${System.nanoTime()}",
                        row = r,
                        col = c,
                        foodType = tile.foodType,
                        specialType = special
                    )
                } else if (coord in finalExplodingCoords) {
                    tile.copy(isExploding = true, isMatched = true)
                } else {
                    tile
                }
            }
        }

        return ExplosionResolution(updatedBoard, finalExplodingCoords)
    }

    private fun determineBiryaniTarget(
        board: Array<Array<Tile>>,
        bombCoord: Pair<Int, Int>,
        explodedSoFar: Set<Pair<Int, Int>>
    ): FoodType {
        // Find adjacent cells (left, right, up, down) that are not exploding
        val neighbors = listOf(
            Pair(bombCoord.first - 1, bombCoord.second),
            Pair(bombCoord.first + 1, bombCoord.second),
            Pair(bombCoord.first, bombCoord.second - 1),
            Pair(bombCoord.first, bombCoord.second + 1)
        ).filter { it.first in 0 until rows && it.second in 0 until cols && it !in explodedSoFar }

        if (neighbors.isNotEmpty()) {
            return board[neighbors.first().first][neighbors.first().second].foodType
        }

        // Fallback to most dominant food on board
        val counts = mutableMapOf<FoodType, Int>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (Pair(r, c) !in explodedSoFar) {
                    val ft = board[r][c].foodType
                    counts[ft] = (counts[ft] ?: 0) + 1
                }
            }
        }
        return counts.maxByOrNull { it.value }?.key ?: FoodType.SAMOSA
    }

    // Moves non-empty tiles down and returns the newly aligned gravity board + count of dropped additions
    fun applyGravityAndRefill(
        board: Array<Array<Tile>>,
        allowedTypes: List<FoodType>
    ): GravityRefillResult {
        val nextBoard = Array(rows) { r ->
            Array(cols) { c ->
                // Temporary mock tile
                board[r][c]
            }
        }

        // For each column
        for (c in 0 until cols) {
            // Read column from bottom (rows-1) to top (0)
            val columnTiles = mutableListOf<Tile>()
            for (r in (rows - 1) downTo 0) {
                if (!board[r][c].isMatched) {
                    columnTiles.add(board[r][c])
                }
            }

            // How many new tiles needed?
            val needed = rows - columnTiles.size

            // Create new tiles
            val refillTiles = mutableListOf<Tile>()
            for (i in 0 until needed) {
                // Initial negative offset for nice entering fall
                val startingYOffset = -400f - (i * 120f)
                val newTile = createRandomTile(-1, c, allowedTypes).copy(
                    startYOffset = startingYOffset,
                    currentYOffset = startingYOffset
                )
                refillTiles.add(newTile)
            }

            // Combine them: refills at the top (which are added in reverse descending order), then columnTiles
            val finalCol = refillTiles.reversed() + columnTiles

            // Save items in nextBoard with proper row indices
            for (r in 0 until rows) {
                val origin = finalCol[r]
                val originalRowOffset = if (origin.row == -1) origin.startYOffset else {
                    // It fell from its original row
                    val diff = (origin.row - r).toFloat()
                    if (diff > 0) -diff * 100f else 0f
                }

                nextBoard[r][c] = Tile(
                    id = origin.id,
                    row = r,
                    col = c,
                    foodType = origin.foodType,
                    specialType = origin.specialType,
                    startYOffset = originalRowOffset,
                    currentYOffset = originalRowOffset
                )
            }
        }

        return GravityRefillResult(nextBoard)
    }

    // Check if swap is adjacent
    fun isAdjacent(p1: Pair<Int, Int>, p2: Pair<Int, Int>): Boolean {
        val rDiff = abs(p1.first - p2.first)
        val cDiff = abs(p1.second - p2.second)
        return (rDiff == 1 && cDiff == 0) || (rDiff == 0 && cDiff == 1)
    }
}

data class MatchResult(
    val matchedCoords: Set<Pair<Int, Int>>,
    val specialsToCreate: Map<Pair<Int, Int>, SpecialType>
)

data class ExplosionResolution(
    val board: Array<Array<Tile>>,
    val fullyExplodedCoords: Set<Pair<Int, Int>>
)

data class GravityRefillResult(
    val board: Array<Array<Tile>>
)
