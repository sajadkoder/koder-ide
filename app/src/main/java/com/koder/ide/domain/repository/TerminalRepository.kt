package com.koder.ide.domain.repository

import com.koder.ide.domain.model.TerminalOutput

interface TerminalRepository {
    suspend fun execute(command: String): TerminalOutput
    suspend fun startSession(): Boolean
    suspend fun stopSession()
    fun isSessionRunning(): Boolean
}
