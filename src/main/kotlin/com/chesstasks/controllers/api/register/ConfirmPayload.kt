package com.chesstasks.controllers.api.register

data class ConfirmPayload(val emailAddress: String, val code: String)