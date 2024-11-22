package com.xeniatechnologies.app.templekiosktirupati

import android.app.Application
import com.xenia.templekiosk.di.roomModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class XeniaTTDTempleKiosk : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@XeniaTTDTempleKiosk)
            modules(roomModule)
        }
    }
}