package com.lr.core.datastore.di

import com.lr.core.datastore.CookieStorage
import com.lr.core.datastore.ProfileStorage
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val datastoreModule = module {
    single { CookieStorage(androidContext()) }
    single { ProfileStorage(androidContext()) }
}
