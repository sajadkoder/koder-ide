package com.koder.ide.domain.model

data class TerminalSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    var title: String = "Shell",
    var isRunning: Boolean = false
)

data class TerminalOutput(
    val text: String,
    val isError: Boolean = false
)
