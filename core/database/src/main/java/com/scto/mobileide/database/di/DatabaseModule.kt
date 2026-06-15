package com.scto.mobileide.database.di

import com.scto.mobileide.core.network.server.MobileServerConfig
import com.scto.mobileide.core.network.api.UserContentApiClient
import com.scto.mobileide.core.user.UserContentRepository
import com.scto.mobileide.database.impl.HybridUserContentRepository
import com.scto.mobileide.database.user.UserContentDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * 数据库模块 DI 配置
 *
 * 架构说明：
 * - 提供 UserContentDatabase 单例
 * - 提供 DAO 实例
 * - 提供 UserContentRepository 实现（混合本地+远程）
 * - 遵循依赖倒置原则（DIP）：app 层依赖接口，不依赖具体实现
 */
val databaseModule = module {

    // 提供 UserContentDatabase 单例
    single {
        UserContentDatabase.getInstance(androidContext())
    }

    // 提供 FavoriteDao
    single {
        get<UserContentDatabase>().favoriteDao()
    }

    // 提供 DownloadHistoryDao
    single {
        get<UserContentDatabase>().downloadHistoryDao()
    }

    // 提供 UserContentApiClient
    single {
        val baseUrl = MobileServerConfig.getInstance(androidContext()).getDefaultServerUrl()
        UserContentApiClient.getInstance(baseUrl)
    }

    // 提供 UserContentRepository 实现（混合本地+远程）
    single<UserContentRepository> {
        HybridUserContentRepository(
            favoriteDao = get(),
            downloadHistoryDao = get(),
            apiClient = get()
        )
    }
}
