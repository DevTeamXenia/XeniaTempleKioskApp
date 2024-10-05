package com.xenia.templekiosk.di

import com.xenia.templekiosk.data.repository.PaymentRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val roomModule = module {

    //single { SessionManager(androidContext()) }

    single { PaymentRepository() }


}