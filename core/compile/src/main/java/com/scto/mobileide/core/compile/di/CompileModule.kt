package com.scto.mobileide.core.compile.di

import com.scto.mobileide.core.compile.CompileTimeoutConfig
import com.scto.mobileide.core.compile.ProcessManager
import com.scto.mobileide.core.compile.artifact.ArtifactStore
import com.scto.mobileide.core.compile.artifact.FingerprintCalculator
import com.scto.mobileide.core.compile.artifact.JsonArtifactStore
import com.scto.mobileide.core.compile.event.BuildEventEmitter
import com.scto.mobileide.core.compile.event.SharedFlowBuildEventEmitter
import com.scto.mobileide.core.compile.launcher.DebugLauncher
import com.scto.mobileide.core.compile.launcher.SdlLauncher
import com.scto.mobileide.core.compile.launcher.TerminalLauncher
import com.scto.mobileide.core.compile.pipeline.BuildExecutor
import com.scto.mobileide.core.compile.pipeline.BuildContextFactory
import com.scto.mobileide.core.compile.pipeline.BuildOrchestrator
import com.scto.mobileide.core.compile.pipeline.BuildPlanner
import com.scto.mobileide.core.compile.pipeline.EnvironmentValidator
import com.scto.mobileide.core.compile.pipeline.LaunchDispatcher
import com.scto.mobileide.core.compile.strategy.BuildStrategy
import com.scto.mobileide.core.compile.strategy.BuildStrategyRegistry
import com.scto.mobileide.core.compile.strategy.CMakeStrategy
import com.scto.mobileide.core.compile.strategy.MakeStrategy
import com.scto.mobileide.core.compile.strategy.SingleFileStrategy
import com.scto.mobileide.core.compile.strategy.GradleStrategy
import com.scto.mobileide.core.linux.LinuxEnvironmentProvider
import com.scto.mobileide.core.linux.UnavailableLinuxEnvironmentProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Core compile 模块的 DI 注册。
 *
 * - Artifact 层、FingerprintCalculator、事件总线 以 `single` 注册(跨 request 共享)
 * - Strategy 以 `single` 注册(内部持有懒加载的 executor,share 一致提升复用率)
 * - Pipeline 组件(Validator/Planner/Executor/Dispatcher/Orchestrator) 以 `factory` 注册
 * - 每次编译请求拿到独立的 [BuildOrchestrator],避免把无关项目全局串行化
 */
val compileModule = module {
    // 进程管理:供 UI 和 AI 执行工具控制当前运行进程
    single { ProcessManager() }

    // ---------- 产物层 ----------
    single<ArtifactStore> { JsonArtifactStore() }
    single { FingerprintCalculator() }

    // ---------- 事件总线(concrete + interface 双暴露,便于 UI 订阅 flow) ----------
    single { SharedFlowBuildEventEmitter() }
    single<BuildEventEmitter> { get<SharedFlowBuildEventEmitter>() }

    // ---------- 共享的超时配置 ----------
    single { CompileTimeoutConfig(androidContext()) }

    // ---------- 终端命令组装器(供 CompileProjectUseCase + UI 层复用) ----------
    single { com.scto.mobileide.core.compile.TerminalCommandBuilder(androidContext()) }

    // ---------- 策略 ----------
    single { SingleFileStrategy(androidContext(), get()) }
    single {
        CMakeStrategy(
            context = androidContext(),
            linuxEnvironmentProvider = getOrNull<LinuxEnvironmentProvider>() ?: UnavailableLinuxEnvironmentProvider,
            timeoutConfig = get(),
        )
    }
    single {
        MakeStrategy(
            context = androidContext(),
            linuxEnvironmentProvider = getOrNull<LinuxEnvironmentProvider>() ?: UnavailableLinuxEnvironmentProvider,
            timeoutConfig = get(),
        )
    }
    single {
        GradleStrategy(
            context = androidContext(),
            linuxEnvironmentProvider = getOrNull<LinuxEnvironmentProvider>() ?: UnavailableLinuxEnvironmentProvider,
        )
    }
    single {
        BuildStrategyRegistry(
            strategies = listOf<BuildStrategy>(
                get<SingleFileStrategy>(),
                get<CMakeStrategy>(),
                get<MakeStrategy>(),
                get<GradleStrategy>(),
            )
        )
    }

    // ---------- Launcher(P3 stub;P4 接入真实启动逻辑) ----------
    single { SdlLauncher() }
    single { DebugLauncher() }
    single { TerminalLauncher() }

    // ---------- Pipeline 无状态组件 ----------
    single { BuildContextFactory() }
    factory { EnvironmentValidator() }
    factory { BuildPlanner(get(), get(), get()) }
    factory { BuildExecutor() }
    factory {
        LaunchDispatcher(
            sdlLauncher = get(),
            debugLauncher = get(),
            terminalLauncher = get(),
        )
    }

    // ---------- 顶层编排器 ----------
    factory {
        BuildOrchestrator(
            validator = get(),
            planner = get(),
            executor = get(),
            dispatcher = get(),
            artifactStore = get(),
            events = get(),
        )
    }
}
