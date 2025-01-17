package com.xenia.churchkiosk

import android.app.Application
import com.xenia.churchkiosk.di.roomModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class XeniaTempleChurch : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@XeniaTempleChurch)
            modules(roomModule)
        }
    }
}