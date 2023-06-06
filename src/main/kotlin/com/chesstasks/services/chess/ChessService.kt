package com.chesstasks.services.chess

import com.github.bhlangonijr.chesslib.Board
import org.koin.core.annotation.Single

@Single
class ChessService {
    fun validatePose(fen: String, vararg moves: String): Boolean {
        return try {
            Board()
                .apply { loadFromFen(fen) }
                .apply { moves.forEach { mv -> doMove(mv) } }
                .let { !it.isMated && !it.isDraw && !it.isStaleMate && !it.isInsufficientMaterial && !it.isRepetition }
        } catch (e: Exception) {
            false
        }
    }
}