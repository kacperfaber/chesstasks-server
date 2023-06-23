package com.chesstasks.data.expressions

import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.QueryBuilder

object Random : Expression<Nothing>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append("RAND()")
    }
}