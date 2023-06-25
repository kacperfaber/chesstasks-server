package com.chesstasks.websocket

import com.chesstasks.data.dto.PuzzleDto

class PuzzleMessage(val fen: String, val firstMove: String)

fun PuzzleDto.toPuzzleMessage() = PuzzleMessage(fen, moves.split(" ").first())