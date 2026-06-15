package com.scto.mobileide.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.gyf.immersionbar.ktx.immersionBar
import com.scto.mobileide.core.config.Prefs
import com.scto.mobileide.ui.compose.screens.main.profile.DownloadHistoryScreen
import com.scto.mobileide.ui.compose.screens.main.profile.FavoritesScreen
import com.scto.mobileide.ui.theme.MobileIDETheme

/**
 * 用户内容 Activity
 *
 * 承载收藏和下载历史界面
 */
class UserContentActivity : ComponentActivity() {

    companion object {
        const val EXTRA_CONTENT_TYPE = "extra_content_type"
        const val TYPE_FAVORITES = "favorites"
        const val TYPE_DOWNLOAD_HISTORY = "download_history"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Prefs.applyTheme()
        super.onCreate(savedInstanceState)

        // 设置沉浸式状态栏
        immersionBar {
            transparentStatusBar()
            statusBarDarkFont(!Prefs.useDarkMode)
            navigationBarColor(android.R.color.transparent)
            navigationBarDarkIcon(!Prefs.useDarkMode)
        }

        val contentType = intent.getStringExtra(EXTRA_CONTENT_TYPE) ?: TYPE_FAVORITES

        setContent {
            MobileIDETheme {
                when (contentType) {
                    TYPE_FAVORITES -> FavoritesContent()
                    TYPE_DOWNLOAD_HISTORY -> DownloadHistoryContent()
                }
            }
        }
    }

    @Composable
    private fun FavoritesContent() {
        FavoritesScreen(
            onNavigateBack = { finish() }
        )
    }

    @Composable
    private fun DownloadHistoryContent() {
        DownloadHistoryScreen(
            onNavigateBack = { finish() }
        )
    }
}
