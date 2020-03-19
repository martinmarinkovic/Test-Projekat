package com.martinmarinkovic.myapplication.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Note::class],
    version = 1
)
abstract class NoteDatabase : RoomDatabase(){

    abstract fun getNoteDao() : NoteDao

    // The companion object is a singleton, and its members can be accessed directly via the name of the containing
    companion object {

        @Volatile private var instance : NoteDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }
        // Kada u drugoj klasu pozovemo NoteDatabase(activity!!), poziva se invoko method
        // Proverava da li je instanca null, bilduje bazu i inicijalizuje instancu
        // Zatim iz ove instance mozemo da zovemp getNoteDao() koj nam daje NoteDao
        // NoteDatabase(activity!!).getNoteDao().getNote()
        // odakle dalje mozemo da dodajemo note u db, citamo, brisemo..


        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            NoteDatabase::class.java,
            "notedatabase"
        ).build()

    }
}