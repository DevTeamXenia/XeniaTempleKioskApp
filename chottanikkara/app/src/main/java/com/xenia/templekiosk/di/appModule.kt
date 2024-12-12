package com.xenia.templekiosk.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.xenia.templekiosk.data.repository.LoginRepository
import com.xenia.templekiosk.data.repository.PaymentRepository
import com.xenia.templekiosk.data.repository.VazhipaduRepository
import com.xenia.templekiosk.data.room.AppDatabase
import com.xenia.templekiosk.ui.dialogue.CustomInternetAvailabilityDialog
import com.xenia.templekiosk.ui.dialogue.CustomQRPopupDialogue
import com.xenia.templekiosk.ui.dialogue.CustomStarPopupDialogue
import com.xenia.templekiosk.ui.dialogue.CustomVazhipaduQRPopupDialogue

import com.xenia.templekiosk.ui.dialogue.CustomWarningPopupDialog
import com.xenia.templekiosk.utils.SessionManager
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
    factory { CustomStarPopupDialogue() }

    single { provideSharedPreferences(get()) }
}


fun provideSharedPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
}