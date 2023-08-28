package com.chesstasks.services.training.ranking

import org.koin.core.annotation.Single
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

@Single
class RankingCalculator {
    fun getNewRanking(ranking: Int, puzzleRanking: Int, success: Boolean): Int {
        // TODO: (RankingCalculator) It doesn't work like I want this to work...

        val diff = puzzleRanking - ranking
        val diffAbs = if (diff != 0) abs(diff) else 5

        if (success) {
            if (diff > 0) {
                // puzzle 1500
                // user 1000
                // diff = 500
                // diffAbs = 500
                // 1500 + (500 * .05) = 1525
                return ranking + (ceil(diffAbs * 0.05).toInt())
            }

            else {
                // puzzle 1500
                // user 2000
                // diff = -500
                // diffAbs = 500
                // 1500 + (500 * .02) = 1510
                return ranking + ceil((diffAbs * 0.02)).toInt()
            }
        }

        else {
            if (diff < 0) {
                // puzzle 1500
                // user 2000
                // diff = -500
                // diffAbs = 500
                // 1500 + (500 * -0.05) = 1475

                return ranking + (ceil(diffAbs * 0.05) * -1).toInt()
            }

            else {
                // puzzle 1500
                // user 1000
                // diff = 500
                // diffAbs = 500
                // 1500 + (500 * -0.01) = 1495
                return ranking + (ceil((diffAbs * 0.01)) * -1).toInt()
            }
        }
    }
}