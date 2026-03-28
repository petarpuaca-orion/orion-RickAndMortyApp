package com.example.rickandmortyapp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.rickandmortyapp.data.local.dao.CharacterDao
import com.example.rickandmortyapp.data.local.entity.CharacterEntity

@Database(
    entities = [CharacterEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
}