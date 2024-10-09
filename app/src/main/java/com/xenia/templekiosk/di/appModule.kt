package com.xenia.templekiosk.di

import android.content.Context
import android.content.SharedPreferences
import com.xenia.templekiosk.data.repository.LoginRepository
import com.xenia.templekiosk.data.repository.PaymentRepository
import com.xenia.templekiosk.ui.dialogue.CustomQRPopupDialogue
import com.xenia.templekiosk.ui.dialogue.CustomWarningPopupDialog
import com.xenia.templekiosk.utils.SessionManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val roomModule = module {

    single { SessionManager(androidContext()) }
    single { LoginRepository() }
    single { PaymentRepository() }

    factory { CustomWarningPopupDialog() }
    factory { CustomQRPopupDialogue() }

    single { provideSharedPreferences(get()) }

}

fun provideSharedPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
}