package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PostEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FotoFilmDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: FotoFilmDatabase? = null

        fun getInstance(context: Context): FotoFilmDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FotoFilmDatabase::class.java,
                    "fotofilm_local_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
