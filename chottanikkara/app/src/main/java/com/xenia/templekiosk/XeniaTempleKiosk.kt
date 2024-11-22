package com.xenia.templekiosk

import android.app.Application
import com.xenia.templekiosk.di.roomModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class XeniaTempleKiosk : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@XeniaTempleKiosk)
            modules(roomModule)
        }
    }
}