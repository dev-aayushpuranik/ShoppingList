package com.aayush.shoppingapp

import android.app.Application
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ShoppingApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        net.sqlcipher.database.SQLiteDatabase.loadLibs(this)

    }

}