package com.chesstasks.services.puzzle.lichess

import com.chesstasks.data.dao.LichessPuzzleDao
import com.chesstasks.data.dto.LichessPuzzleDto
import org.koin.core.annotation.Single

@Single
class LichessPuzzleService(private val lichessPuzzleDao: LichessPuzzleDao) {
    suspend fun getById(id: String): LichessPuzzleDto? {
        return lichessPuzzleDao.getById(id)
    }
}