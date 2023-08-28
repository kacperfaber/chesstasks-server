package com.chesstasks.services.training.ranking

import org.koin.core.annotation.Single
import kotlin.math.abs
import kotlin.math.roundToInt

@Single
class RankingCalculator {
    fun getNewRanking(ranking: Int, puzzleRanking: Int, success: Boolean): Int {
        // TODO: (RankingCalculator) It doesn't work like I want this to work...

        val diff = puzzleRanking - ranking
        val diffAbs = abs(diff)

        if (success) {
            if (diff > 0) {
                // puzzle 1500
                // user 1000
                // diff = 500
                // diffAbs = 500
                // 1500 + (500 * .05) = 1525
                return ranking + (diffAbs * 0.05).roundToInt()
            }

            else {
                // puzzle 1500
                // user 2000
                // diff = -500
                // diffAbs = 500
                // 1500 + (500 * .02) = 1510
                return ranking + (diffAbs * 0.02).roundToInt()
            }
        }

        else {
            if (diff < 0) {
                // puzzle 1500
                // user 2000
                // diff = -500
                // diffAbs = 500
                // 1500 + (500 * -0.05) = 1475

                return ranking + (diffAbs * -0.05).roundToInt()
            }

            else {
                // puzzle 1500
                // user 1000
                // diff = 500
                // diffAbs = 500
                // 1500 + (500 * -0.01) = 1495
                return ranking + (diffAbs * -0.01).roundToInt()
            }
        }
    }
}