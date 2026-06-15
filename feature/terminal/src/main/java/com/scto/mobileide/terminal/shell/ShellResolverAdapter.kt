package com.scto.mobileide.terminal.shell

import android.content.Context
import com.scto.mobileide.core.terminal.IShellResolver
import com.scto.mobileide.core.terminal.ShellAvailabilityInfo
import com.scto.mobileide.core.terminal.ShellType
import com.scto.mobileide.core.terminal.TerminalBackendType
import com.scto.mobileide.terminal.preferences.TerminalPreferences

/**
 * Shell 解析器适配器
 *
 * 将 TerminalShellResolver 适配为 IShellResolver 接口。
 */
class ShellResolverAdapter(
    private val context: Context
) : IShellResolver {

    private val resolver = TerminalShellResolver(context)

    override suspend fun isShellAvailable(shellType: String): Boolean {
        val type = TerminalPreferences.ShellType.fromValue(shellType)
        return resolver.isShellAvailable(type)
    }

    override suspend fun probeAvailability(): ShellAvailabilityInfo {
        val availability = resolver.probeAvailability()
        return ShellAvailabilityInfo(
            backend = when (availability.backend) {
                TerminalBackend.PROOT -> TerminalBackendType.PROOT
                TerminalBackend.HOST -> TerminalBackendType.HOST
            },
            autoResolved = availability.autoResolved?.let { resolved ->
                ShellType.fromValue(resolved.value)
            },
            availableShells = availability.availableShells.map { shell ->
                ShellType.fromValue(shell.value)
            }
        )
    }

    override fun isPRootInstalled(): Boolean {
        return resolver.isPRootInstalled()
    }
}
