package com.xenia.churchkiosk.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.xenia.churchkiosk.data.repository.LoginRepository
import com.xenia.churchkiosk.data.repository.PaymentRepository
import com.xenia.churchkiosk.data.repository.VazhipaduRepository
import com.xenia.churchkiosk.data.room.AppDatabase
import com.xenia.churchkiosk.ui.dialogue.CustomInternetAvailabilityDialog
import com.xenia.churchkiosk.ui.dialogue.CustomQRPopupDialogue
import com.xenia.churchkiosk.ui.dialogue.CustomVazhipaduQRPopupDialogue
import com.xenia.churchkiosk.ui.dialogue.CustomWarningPopupDialog
import com.xenia.churchkiosk.utils.SessionManager
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val roomModule = module {

    single {
        Room.databaseBuilder(
            androidApplication(),
            AppDatabase::class.java,
            "XeniaTempleDB"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single { get<AppDatabase>().vazhipaduDao() }

    single { SessionManager(androidContext()) }
    single { LoginRepository() }
    single { VazhipaduRepository(get()) }
    single { PaymentRepository() }

    factory { CustomWarningPopupDialog() }
    factory { CustomQRPopupDialogue() }
    factory { CustomVazhipaduQRPopupDialogue() }
    factory { CustomInternetAvailabilityDialog() }

    single { provideSharedPreferences(get()) }
}


fun provideSharedPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
}