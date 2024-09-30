package com.aayush.shoppingapp.di

import android.content.Context
import androidx.room.Room
import com.aayush.shoppingapp.database.SQLCipherUtils
import com.aayush.shoppingapp.database.ShoppingDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Singleton
    @Provides
    fun provideDB(@ApplicationContext context: Context): ShoppingDatabase {
        val userPassphrase = charArrayOf('j', 'a', 'i') // Replace with your passphrase
        val passphrase = SQLiteDatabase.getBytes(userPassphrase)
        val state = SQLCipherUtils.getDatabaseState(context, "ShoppingDatabase.db")


        if (state == SQLCipherUtils.State.UNENCRYPTED) {
            SQLCipherUtils.encrypt(
                context,
                "ShoppingDatabase.db",
                passphrase
            )
        }
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(context, ShoppingDatabase::class.java, "ShoppingDatabase")
            .openHelperFactory(factory)
            .build()
    }
}