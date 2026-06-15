package com.scto.mobileide.search

data class SearchOptions(
    val caseSensitive: Boolean = false,
    val useRegex: Boolean = false,
    val wholeWord: Boolean = false
)

