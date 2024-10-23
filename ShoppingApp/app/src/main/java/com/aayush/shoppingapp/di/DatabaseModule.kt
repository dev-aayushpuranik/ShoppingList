package com.aayush.shoppingapp.di

import android.content.Context
import androidx.room.Room
import com.aayush.shoppingapp.database.SQLCipherUtils
import com.aayush.shoppingapp.database.ShoppingDatabase
import com.aayush.shoppingapp.database.ShoppingDatabasePassphrase
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
    fun providesShoppingDatabasePassphrase(@ApplicationContext context: Context) = ShoppingDatabasePassphrase(context)

    @Singleton
    @Provides
    fun provideSupportFactory(shoppingDatabasePassphrase: ShoppingDatabasePassphrase) = SupportFactory(shoppingDatabasePassphrase.getPassphrase())

    @Singleton
    @Provides
    fun provideDB(@ApplicationContext context: Context, supportFactory: SupportFactory): ShoppingDatabase {
        return Room.databaseBuilder(context, ShoppingDatabase::class.java, "ShoppingDatabase")
            .openHelperFactory(supportFactory)
            .build()
    }
}