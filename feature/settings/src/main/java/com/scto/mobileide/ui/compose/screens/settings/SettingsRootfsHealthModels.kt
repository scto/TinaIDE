package com.scto.mobileide.ui.compose.screens.settings

enum class RootfsHealthStatus {
    UNKNOWN,
    CHECKING,
    READY,
    ATTENTION,
    UNAVAILABLE,
}

data class RootfsHealthUiState(
    val status: RootfsHealthStatus = RootfsHealthStatus.UNKNOWN,
    val statusText: String = "",
    val detailText: String = "",
)
