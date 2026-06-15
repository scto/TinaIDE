package com.scto.mobileide.core.editorview

internal fun EditorState.seedVisibleHover(
    markdown: String = "hover docs"
) {
    publishHoverVisible(markdown)
}
