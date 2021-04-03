package com.sergiocruz.matematica.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope

@Database(entities = [HistoryDataClass::class], version = 1, exportSchema = true)
//@TypeConverters([DateConverter::class, StringListConverter::class])
abstract class LocalDatabase : RoomDatabase() {

    abstract fun historyDAO(): HistoryDAO

    companion object {

        private val LOCK = Any()

        private const val DATABASE_NAME = "matematica_database.db"

        @Volatile
        private var INSTANCE: LocalDatabase? = null

        fun getInstance(context: Context): LocalDatabase {
            synchronized(LOCK) {
                if (INSTANCE == null) {
                    INSTANCE = Room
                            .databaseBuilder(context.applicationContext, LocalDatabase::class.java, DATABASE_NAME)
                            .build()
                }
                return INSTANCE as LocalDatabase
            }
        }

        fun getDatabase(
                context: Context,
                scope: CoroutineScope
        ): LocalDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                return Room
                        .databaseBuilder(
                        context.applicationContext,
                        LocalDatabase::class.java,
                        "word_database"
                )
                        .fallbackToDestructiveMigration()
                        .build()
                        .also { INSTANCE = it }
            }
        }

    }
}