package com.martinmarinkovic.myapplication.roomdb

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.io.Serializable
import java.lang.reflect.Type

@Entity
data class Note(
    @PrimaryKey var id : String = "",
    val title: String? = null,
    val note: String? = null,
    var images: ArrayList<String>? = null,
    var audioFiles: ArrayList<String>? = null
): Serializable

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