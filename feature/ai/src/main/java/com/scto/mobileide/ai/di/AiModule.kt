package com.scto.mobileide.ai.di

import com.scto.mobileide.ai.channel.AiChannelApiKeyStore
import com.scto.mobileide.ai.channel.AiChannelRepository
import com.scto.mobileide.ai.config.AiPreferences
import com.scto.mobileide.ai.repository.ConversationRepository
import com.scto.mobileide.ai.settings.AiSettingsBridgeImpl
import com.scto.mobileide.ai.tools.ToolInitializer
import com.scto.mobileide.ai.viewmodel.AiChatViewModel
import com.scto.mobileide.core.config.ai.AiChannelProvider
import com.scto.mobileide.core.config.ai.AiConfigProvider
import com.scto.mobileide.core.config.ai.AiSettingsBridge
import com.scto.mobileide.database.user.UserContentDatabase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val aiModule = module {
    single { AiPreferences(context = get()) }
    single<AiConfigProvider> { get<AiPreferences>() }

    single { AiChannelApiKeyStore(get()) }
    single { get<UserContentDatabase>().aiChannelDao() }
    single { AiChannelRepository(dao = get(), apiKeyStore = get()) }
    single<AiChannelProvider> { get<AiChannelRepository>() }

    single<AiSettingsBridge> {
        AiSettingsBridgeImpl(
            context = get(),
            aiPreferences = get(),
            channelRepository = get(),
        )
    }
    single { ConversationRepository(get(), get()) }

    // AiChatViewModel 使用 V3 注册模式，不再需要在创建时传入 toolExecutionContext
    // 使用方需要在获取 ViewModel 后调用 initializeProjectContext() 等方法注册回调
    viewModel {
        AiChatViewModel(
            context = get(),
            aiPreferences = get(),
            channelRepository = get(),
            conversationRepository = get()
        )
    }

    // 初始化工具系统（立即初始化）
    single(createdAtStart = true) {
        val aiPreferences = get<AiPreferences>()
        ToolInitializer.apply {
            registerBuiltInTools()
            // 加载保存的工具启用状态
            val savedStates = aiPreferences.loadToolEnabledStates()
            if (savedStates.isNotEmpty()) {
                com.scto.mobileide.ai.tools.ToolRegistry.setToolEnabledStates(savedStates)
            }
        }
    }
}
