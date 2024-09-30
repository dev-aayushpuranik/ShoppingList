package com.aayush.shoppingapp.di

import android.content.Context
import androidx.room.Room
import com.aayush.shoppingapp.database.ShoppingDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Singleton
    @Provides
    fun provideDB(@ApplicationContext context: Context): ShoppingDatabase {
        return Room.databaseBuilder(context, ShoppingDatabase::class.java, "CategoryDatabase").build()
    }
}