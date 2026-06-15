package com.scto.mobileide.core.editorview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier

@Composable
fun MobileEditor(
    state: EditorState,
    modifier: Modifier = Modifier,
    onPerformanceSnapshotReaderChanged: (((() -> EditorRenderPerformanceSnapshot)?) -> Unit)? = null
) {
    val session = rememberMobileEditorSession(state)
    DisposableEffect(session, onPerformanceSnapshotReaderChanged) {
        onPerformanceSnapshotReaderChanged?.invoke {
            session.renderer.performanceSnapshot()
        }
        onDispose {
            onPerformanceSnapshotReaderChanged?.invoke(null)
        }
    }
    MobileEditorScaffold(
        session = session,
        modifier = modifier
    )
}
