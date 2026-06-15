package com.scto.mobileide.editor.di

import com.scto.mobileide.core.editor.IBookmarkRepository
import com.scto.mobileide.core.symbol.IProjectSymbolIndexService
import com.scto.mobileide.editor.EditorManager
import com.scto.mobileide.editor.IEditorManager
import com.scto.mobileide.editor.bookmark.BookmarkRepository
import com.scto.mobileide.editor.bookmark.BookmarkRepositoryAdapter
import com.scto.mobileide.editor.bookmark.BookmarkService
import com.scto.mobileide.editor.symbol.CxxSymbolProvider
import com.scto.mobileide.editor.symbol.JavaSymbolProvider
import com.scto.mobileide.editor.symbol.KotlinSymbolProvider
import com.scto.mobileide.editor.symbol.PythonSymbolProvider
import com.scto.mobileide.editor.symbol.ProjectSymbolIndexService
import com.scto.mobileide.editor.symbol.RustSymbolProvider
import com.scto.mobileide.editor.theme.PluginEditorThemeRegistry
import com.scto.mobileide.file.IProjectContext
import com.scto.mobileide.plugin.EditorThemeIndex
import org.koin.dsl.module

val editorModule = module {
    single { CxxSymbolProvider() }
    single { JavaSymbolProvider() }
    single { KotlinSymbolProvider() }
    single { PythonSymbolProvider() }
    single { RustSymbolProvider() }

    single<IProjectSymbolIndexService> {
        ProjectSymbolIndexService(
            context = get(),
            providers = listOf(
                get<CxxSymbolProvider>(),
                get<JavaSymbolProvider>(),
                get<KotlinSymbolProvider>(),
                get<PythonSymbolProvider>(),
                get<RustSymbolProvider>(),
            ),
        )
    }

    // BookmarkRepository（feature:editor 层接口）
    single<BookmarkRepository> { BookmarkService(get()) }

    // IBookmarkRepository（core:common 层接口）
    single<IBookmarkRepository> { BookmarkRepositoryAdapter(get()) }

    single { PluginEditorThemeRegistry(get(), get()).also { it.onCreate() } }
    single<EditorThemeIndex> { get<PluginEditorThemeRegistry>() }
    single<IEditorManager> {
        EditorManager(
            context = get(),
            configManager = get(),
            projectContextProvider = { getKoin().getOrNull<IProjectContext>() },
            projectSymbolIndexServiceProvider = { getKoin().getOrNull() },
        ).also { it.onCreate() }
    }
}
