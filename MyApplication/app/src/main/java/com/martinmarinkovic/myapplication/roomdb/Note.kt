package com.martinmarinkovic.myapplication.roomdb

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.io.Serializable
import java.lang.reflect.Type


@Entity // db table
data class Note(
    // columns
    val title: String,
    val note: String,

    val images: ArrayList<String>
    //var image: String? = null

): Serializable {
    @PrimaryKey(autoGenerate = true)
    var id : Int? = null
}

class Converters {
    @TypeConverter
    fun fromString(value: String?): ArrayList<String> {
        val listType: Type = object : TypeToken<ArrayList<String?>?>() {}.getType()
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<String?>?): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}