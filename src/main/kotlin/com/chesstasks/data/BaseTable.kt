package com.chesstasks.data

import org.jetbrains.exposed.sql.Table
import kotlin.random.Random

open class BaseTable(tableName: String) : Table(tableName) {
    val id = integer("id").clientDefault { Random.nextInt() }

    val createdAt = long("created_at").clientDefault { System.currentTimeMillis() }

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}