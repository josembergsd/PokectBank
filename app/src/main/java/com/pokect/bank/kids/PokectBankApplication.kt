package com.pokect.bank.kids

import android.app.Application
import com.pokect.bank.kids.data.di.AppContainer

class PokectBankApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
