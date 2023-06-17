package com.chesstasks.data.dao

import com.chesstasks.data.DatabaseFactory.dbQuery
import com.chesstasks.data.dto.*
import com.chesstasks.data.dto.PuzzleDto.Companion.from
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.koin.core.annotation.Single

@Single
class PuzzleDao {
    suspend fun getAll(limit: Int, skip: Long): List<PuzzleDto> = dbQuery {
        Puzzles
            .join(Users, JoinType.LEFT, additionalConstraint = { Puzzles.ownerId eq Users.id })
            .join(PuzzleThemes, JoinType.LEFT, additionalConstraint = { PuzzleThemes.puzzleId eq Puzzles.id })
            .join(Themes, JoinType.LEFT, additionalConstraint = { PuzzleThemes.themeId eq Themes.id })
            .join(Openings, JoinType.LEFT, additionalConstraint = {Puzzles.openingId eq Openings.id})
            .selectAll()
            .limit(limit, skip)
            .map(::from)
    }


    suspend fun getByOwnerId(ownerId: Int, limit: Int = 50, skip: Long = 50): List<PuzzleDto> = dbQuery {
        Puzzles
            .join(Users, JoinType.LEFT, additionalConstraint = { Puzzles.ownerId eq Users.id })
            .join(PuzzleThemes, JoinType.LEFT, additionalConstraint = { PuzzleThemes.puzzleId eq Puzzles.id })
            .join(Themes, JoinType.LEFT, additionalConstraint = { PuzzleThemes.themeId eq Themes.id })
            .join(Openings, JoinType.LEFT, additionalConstraint = {Puzzles.openingId eq Openings.id})
            .select { Puzzles.ownerId eq ownerId }
            .limit(limit, skip)
            .map(::from)
    }

    suspend fun getById(id: Int): PuzzleDto? = dbQuery {
        Puzzles
            .join(Users, JoinType.LEFT, additionalConstraint = { Puzzles.ownerId eq Users.id })
            .join(PuzzleThemes, JoinType.LEFT, additionalConstraint = { PuzzleThemes.puzzleId eq Puzzles.id })
            .join(Themes, JoinType.LEFT, additionalConstraint = { PuzzleThemes.themeId eq Themes.id })
            .join(Openings, JoinType.LEFT, additionalConstraint = {Puzzles.openingId eq Openings.id})
            .select { Puzzles.id eq id }
            .map(::from)
            .firstOrNull()
    }

    suspend fun getAllByDatabase(database: PuzzleDatabase, limit: Int, skip: Long): List<PuzzleDto> = dbQuery {
        Puzzles
            .join(Users, JoinType.LEFT, additionalConstraint = { Puzzles.ownerId eq Users.id })
            .join(PuzzleThemes, JoinType.LEFT, additionalConstraint = { PuzzleThemes.puzzleId eq Puzzles.id })
            .join(Themes, JoinType.LEFT, additionalConstraint = { PuzzleThemes.themeId eq Themes.id })
            .join(Openings, JoinType.LEFT, additionalConstraint = {Puzzles.openingId eq Openings.id})
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
        Puzzles.join(Users, JoinType.LEFT, additionalConstraint = {Puzzles.ownerId eq Users.id})
            .join(PuzzleThemes, JoinType.LEFT, additionalConstraint = {PuzzleThemes.puzzleId eq Puzzles.id})
            .join(Themes, JoinType.LEFT, additionalConstraint = {Themes.id eq PuzzleThemes.themeId})
            .join(Openings, JoinType.LEFT, additionalConstraint = {Puzzles.openingId eq Openings.id})
            .select { Themes.name eq themeName }
            .limit(limit, skip)
            .map(PuzzleDto::from)
    }
}