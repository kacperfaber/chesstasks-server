package com.chesstasks.data.dao

import com.chesstasks.data.DatabaseFactory.dbQuery
import com.chesstasks.data.dto.*
import com.chesstasks.data.dto.PuzzleDto.Companion.from
import com.chesstasks.data.expressions.Random
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inSubQuery
import org.koin.core.annotation.Single

@Single
class PuzzleDao {
    private suspend fun Puzzles.prepareJoin(): Join {
        return Puzzles
            .join(Users, JoinType.LEFT, additionalConstraint = { Puzzles.ownerId eq Users.id })
            .join(PuzzleThemes, JoinType.LEFT, additionalConstraint = { PuzzleThemes.puzzleId eq Puzzles.id })
            .join(Themes, JoinType.LEFT, additionalConstraint = { PuzzleThemes.themeId eq Themes.id })
            .join(Openings, JoinType.LEFT, additionalConstraint = { Puzzles.openingId eq Openings.id })
    }

    suspend fun getAll(limit: Int, skip: Long): List<PuzzleDto> = dbQuery {
        Puzzles.prepareJoin()
            .selectAll()
            .limit(limit, skip)
            .map(::from)
    }


    suspend fun getByOwnerId(ownerId: Int, limit: Int = 50, skip: Long = 50): List<PuzzleDto> = dbQuery {
        Puzzles
            .prepareJoin()
            .select { Puzzles.ownerId eq ownerId }
            .limit(limit, skip)
            .map(::from)
    }

    suspend fun getById(id: Int): PuzzleDto? = dbQuery {
        Puzzles
            .prepareJoin()
            .select { Puzzles.id eq id }
            .map(::from)
            .firstOrNull()
    }

    suspend fun getAllByDatabase(database: PuzzleDatabase, limit: Int, skip: Long): List<PuzzleDto> = dbQuery {
        Puzzles
            .prepareJoin()
            .select { Puzzles.database eq database }
            .limit(limit, skip).map(::from)
    }

    suspend fun deleteByIdAndAuthenticatedUserId(id: Int, authenticatedUserId: Int): Boolean = dbQuery {
        Puzzles.deleteWhere { (Puzzles.id eq id) and (Puzzles.ownerId eq authenticatedUserId) } > 0
    }

    suspend fun deleteById(id: Int): Boolean = dbQuery {
        Puzzles.deleteWhere { Puzzles.id eq id } > 0
    }

    suspend fun getAllByThemeName(themeName: String, limit: Int, skip: Long): List<PuzzleDto> = dbQuery {
        Puzzles.prepareJoin()
            .select { Themes.name eq themeName }
            .limit(limit, skip)
            .map(PuzzleDto::from)
    }

    suspend fun getAllByOpeningId(openingId: Int, limit: Int, skip: Long): List<PuzzleDto> = dbQuery {
        Puzzles.prepareJoin()
            .select { Openings.id eq openingId }
            .limit(limit, skip)
            .map(PuzzleDto::from)
    }

    suspend fun getAllByOpeningEco(openingEco: String, limit: Int, skip: Long): List<PuzzleDto> = dbQuery {
        Puzzles.prepareJoin()
            .select { Openings.eco eq openingEco }
            .limit(limit, skip)
            .map(PuzzleDto::from)
    }

    suspend fun getAllUnresolved(userId: Int) = dbQuery {
        (Puzzles).prepareJoin()
            .join(PuzzleHistoryItems, JoinType.LEFT, additionalConstraint = {PuzzleHistoryItems.puzzleId eq Puzzles.id})
            .select {
                PuzzleHistoryItems.id notInSubQuery
                        PuzzleHistoryItems
                            .select { (PuzzleHistoryItems.puzzleId eq Puzzles.id) }
                            .having { PuzzleHistoryItems.id.count() eq 0 }
            }
            .limit(50, 0)
            .map(PuzzleDto::from)
    }

    suspend fun getRandomByRankingRange(min: Int, max: Int, limit: Int, skip: Long) = dbQuery {
        Puzzles
            .prepareJoin()
            .select { (Puzzles.ranking lessEq max) and (Puzzles.ranking greaterEq min) }
            .limit(limit, skip)
            .orderBy(com.chesstasks.data.expressions.Random)
            .map(PuzzleDto::from)
    }

    suspend fun getList(where: List<Op<Boolean>>, limit: Int, skip: Long): List<PuzzleDto> = dbQuery{
        if (where.isEmpty()) throw RuntimeException("PuzzleDao.getList: 'where' parameter can't be an empty list.")

        Puzzles
            .prepareJoin()
            .select {where.reduce {acc, op -> acc.and(op)}}
            .limit(limit, skip)
            .map(PuzzleDto::from)
    }

    suspend fun getRandomList(where: List<Op<Boolean>>, limit: Int, skip: Long): List<PuzzleDto> = dbQuery{
        if (where.isEmpty()) throw RuntimeException("PuzzleDao.getList: 'where' parameter can't be an empty list.")

        Puzzles
            .prepareJoin()
            .select {where.reduce {acc, op -> acc.and(op)}}
            .limit(limit, skip)
            .orderBy(Random)
            .map(PuzzleDto::from)
    }

    data class SearchPuzzlesCriteria(
        val themeIds: List<Int>,
        val rankingFrom: Int,
        val rankingTo: Int
    )

    suspend fun searchPuzzles(c: SearchPuzzlesCriteria, limit: Int): List<PuzzleDto> = dbQuery {
        val (themeIds, rankingFrom, rankingTo) = c

        val themeIdsQuery = ((Puzzles.id inSubQuery PuzzleThemes
            .slice(PuzzleThemes.puzzleId)
            .select { PuzzleThemes.themeId inList themeIds }
            .groupBy(PuzzleThemes.puzzleId)
            .having { PuzzleThemes.puzzleId.count() eq themeIds.size.toLong() }))

        if (c.themeIds.isEmpty()) {
            return@dbQuery Puzzles
                .select { (Puzzles.ranking greaterEq rankingFrom) and (Puzzles.ranking lessEq rankingTo) }
                .orderBy(Random)
                .limit(limit)
                .map(PuzzleDto::from)
        }

        Puzzles
            .select { themeIdsQuery and (Puzzles.ranking greaterEq rankingFrom) and (Puzzles.ranking lessEq rankingTo) }
            .orderBy(Random)
            .limit(limit)
            .map(PuzzleDto::from)
    }

    suspend fun getPuzzleRanking(puzzleId: Int): Int? = dbQuery {
        Puzzles
            .slice(Puzzles.id, Puzzles.ranking)
            .select { Puzzles.id eq puzzleId }
            .map { it[Puzzles.ranking] }
            .singleOrNull()
    }
}