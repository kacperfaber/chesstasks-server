package com.chesstasks.services.puzzle.user

import com.chesstasks.data.dao.UserPuzzleDao
import com.chesstasks.data.dto.UserPuzzleDto
import org.koin.core.annotation.Single

@Single
class UserPuzzleService(private val userPuzzleDao: UserPuzzleDao) {
    suspend fun getById(id: Int): UserPuzzleDto? = userPuzzleDao.getById(id)
    suspend fun getByOwnerId(ownerId: Int): List<UserPuzzleDto> = userPuzzleDao.getByOwnerId(ownerId)
    suspend fun getByIdAndOwnerId(id: Int, ownerId: Int) = userPuzzleDao.getByIdAndOwnerId(id, ownerId)
    suspend fun createNew(ownerId: Int, fen: String, moves: String, ranking: Int): UserPuzzleDto? = userPuzzleDao.createNew(ownerId, fen, moves, ranking)
    suspend fun deleteAsOwner(ownerId: Int, id: Int): Boolean = userPuzzleDao.deleteByIdAndOwnerId(id, ownerId)
    suspend fun deleteById(id: Int): Boolean = userPuzzleDao.deleteById(id)
}