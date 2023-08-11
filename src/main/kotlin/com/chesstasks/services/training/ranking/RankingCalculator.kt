package com.chesstasks.services.training.ranking

import org.koin.core.annotation.Single
import kotlin.math.roundToInt

@Single
class RankingCalculator {
    fun getNewRanking(ranking: Int, puzzleRanking: Int, success: Boolean): Int {
        // TODO: (RankingCalculator) It doesn't work like I want this to work...

        val inc = if (success) 1 else -1
        val diff = puzzleRanking - ranking
        return ((diff * inc) * 0.01).roundToInt()
    }
}