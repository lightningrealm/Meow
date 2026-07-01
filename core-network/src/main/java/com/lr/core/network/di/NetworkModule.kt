package com.lr.core.network.di

import com.lr.core.network.AuthInterceptor
import com.lr.core.network.MeowApiService
import com.lr.core.network.PersistentCookieJar
import com.lr.core.network.api.MeowAuthService
import com.lr.core.network.api.MeowUserService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://www.lightningrealm.cloud/"

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
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
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
}
