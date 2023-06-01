package com.chesstasks.services.puzzle.lichess

import com.chesstasks.data.dao.LichessPuzzleDao
import com.chesstasks.data.dto.LichessPuzzleDto
import org.koin.core.annotation.Single

@Single
class LichessPuzzleService(private val lichessPuzzleDao: LichessPuzzleDao) {
    suspend fun getById(id: String): LichessPuzzleDto? {
        return lichessPuzzleDao.getById(id)
    }

    suspend fun createNew(id: String, fen: String, moves: String, ranking: Int): LichessPuzzleDto? {
       return lichessPuzzleDao.insert(id, fen, moves, ranking)
    }

    suspend fun deleteById(id: String): Boolean {
        return lichessPuzzleDao.deleteById(id)
    }
}