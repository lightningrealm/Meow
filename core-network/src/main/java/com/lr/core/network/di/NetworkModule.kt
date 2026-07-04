package com.lr.core.network.di

import com.lr.core.network.AuthInterceptor
import com.lr.core.network.MeowApiService
import com.lr.core.network.PersistentCookieJar
import com.lr.core.network.api.MeowAuthService
import com.lr.core.network.api.MeowPlaylistService
import com.lr.core.network.api.MeowRecommendService
import com.lr.core.network.api.MeowSearchService
import com.lr.core.network.api.MeowSongService
import com.lr.core.network.api.MeowUserService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

private const val BASE_URL = "http://112.124.4.51:3000/"

val networkModule = module {

    single {
        PersistentCookieJar(cookieStorage = get())
    }

    single {
        AuthInterceptor(cookieJar = get())
    }

    single {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(get<AuthInterceptor>())
            .cookieJar(get<PersistentCookieJar>())
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    single {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    single {
        get<Retrofit>().create(MeowApiService::class.java)
    }

    single {
        get<Retrofit>().create(MeowAuthService::class.java)
    }

    single {
        get<Retrofit>().create(MeowUserService::class.java)
    }

    single {
        get<Retrofit>().create(MeowRecommendService::class.java)
    }

    single {
        get<Retrofit>().create(MeowSearchService::class.java)
    }

    single {
        get<Retrofit>().create(MeowPlaylistService::class.java)
    }

    single {
        get<Retrofit>().create(MeowSongService::class.java)
    }
}
