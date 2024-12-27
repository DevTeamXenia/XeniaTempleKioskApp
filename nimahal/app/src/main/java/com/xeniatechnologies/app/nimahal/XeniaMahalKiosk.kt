package com.xeniatechnologies.app.nimahal

import android.app.Application
import com.xenia.templekiosk.di.roomModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class XeniaMahalKiosk : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@XeniaMahalKiosk)
            modules(roomModule)
        }
    }
}