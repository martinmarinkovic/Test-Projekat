package com.martinmarinkovic.myapplication.roomdb

import androidx.room.*

@Dao
interface NoteDao {

    @Insert
    suspend fun addNote(note: Note)

    @Query("SELECT * FROM note ORDER BY id DESC")
    suspend fun getAllNotes() : List<Note>

    @Insert
    suspend fun addMultipleNotes(vararg note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

   /* @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertByReplacement(image: List<Image>)

    @Query("SELECT * FROM image")
    fun getAll(): List<Image>

    @Delete
    fun delete(image: Image)*/
}