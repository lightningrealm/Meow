package com.lr.meow

import android.app.Application
import android.content.Context
import com.lr.meow.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import com.lr.core.datastore.di.datastoreModule
import com.lr.core.network.di.networkModule
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class Myapp: Application() {
    private lateinit var instance: Myapp
    fun getInstance(): Myapp = instance
    fun getAppContext(): Context = instance.applicationContext

    override fun onCreate() {
        super.onCreate()
        instance = this
        initKoin()
    }

    private fun initKoin(){
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@Myapp)
            modules(
                datastoreModule,
                networkModule,
                appModule
            )
        }
    }
}