package com.martinmarinkovic.myapplication.roomdb

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity // db table
data class Note(
    // columns
    val title: String,
    val note: String
): Serializable {
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0
}