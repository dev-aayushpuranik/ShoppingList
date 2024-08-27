package com.aayush.shoppingapp.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aayush.shoppingapp.database.dao.SubcategoryDao
import com.aayush.shoppingapp.database.entities.SubcategoryTable


//var MIGRATION_3_4: Migration = object : Migration(3, 4) {
//    override fun migrate(database: SupportSQLiteDatabase) {
//        database.execSQL("ALTER TABLE 'SubcategoryTable' ADD COLUMN 'remind_at' Long")
//    }
//}
@Database(entities = [SubcategoryTable::class], version = 4,
    autoMigrations = [AutoMigration(from = 3, to = 4)], exportSchema = true)
abstract class SubCategoryDatabase : RoomDatabase() {

    abstract fun SubcategoryDao(): SubcategoryDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: SubCategoryDatabase? = null

        fun getDatabase(context: Context): SubCategoryDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SubCategoryDatabase::class.java,
                    "SubCategoryDatabase"
                )
//                    .addMigrations(MIGRATION_3_4)
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}