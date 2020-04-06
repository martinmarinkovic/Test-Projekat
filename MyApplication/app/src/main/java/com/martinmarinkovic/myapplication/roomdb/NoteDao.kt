package com.martinmarinkovic.myapplication.roomdb

import androidx.room.*

@Dao
interface NoteDao {

    @Insert
    suspend fun addNote(note: Note)

    //@Query("SELECT * FROM note ORDER BY id DESC")
    @Query("SELECT * FROM note ORDER BY date DESC")
    suspend fun getAllNotes() : List<Note>

    @Insert
    suspend fun addMultipleNotes(vararg note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)
}