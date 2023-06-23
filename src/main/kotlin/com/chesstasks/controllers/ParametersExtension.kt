package com.chesstasks.controllers

import io.ktor.http.*

fun Parameters.getValue(name: String): String = this[name] ?: throw Exception("Missing form parameter '$name'")