package com.xenia.templekiosk.di

import android.content.Context
import android.content.SharedPreferences
import com.xeniatechnologies.app.nimahal.data.repository.PaymentRepository
import com.xeniatechnologies.app.nimahal.dialogue.CustomInternetAvailabilityDialog
import org.koin.dsl.module

val roomModule = module {
    single { provideSharedPreferences(get()) }
    single { PaymentRepository() }

    factory { CustomInternetAvailabilityDialog() }
}


fun provideSharedPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
}