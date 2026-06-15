package com.scto.mobileide.core.network.server

import com.scto.mobileide.core.network.OkHttpClientProvider
import okhttp3.OkHttpClient

object MobileServerHttpClientFactory {
    fun anonymous(
        baseClient: OkHttpClient = OkHttpClientProvider.default,
        configure: OkHttpClient.Builder.() -> Unit = {}
    ): OkHttpClient {
        return baseClient.newBuilder()
            .apply(configure)
            .build()
    }
}
