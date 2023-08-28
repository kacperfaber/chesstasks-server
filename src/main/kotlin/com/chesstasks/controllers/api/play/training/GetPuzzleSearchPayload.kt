package com.chesstasks.controllers.api.play.training

import com.chesstasks.data.dao.PuzzleDao

data class RankingRange(val from: Int, val to: Int)

data class GetPuzzleSearchPayload(
    val ranking: RankingRange,
    val themeIds: List<Int>,
    val skipSolved: Boolean
) {
    fun toSearchPuzzleCriteria(): PuzzleDao.SearchPuzzlesCriteria{
        return PuzzleDao.SearchPuzzlesCriteria(themeIds, ranking.from, ranking.to)
    }
}