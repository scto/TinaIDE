package com.scto.mobileide.core.config

enum class NewProjectSourceLocation(val value: String) {
    PUBLIC("public"),
    PRIVATE("private");

    companion object {
        fun fromValue(raw: String?): NewProjectSourceLocation {
            return entries.firstOrNull { it.value == raw } ?: PUBLIC
        }
    }
}
