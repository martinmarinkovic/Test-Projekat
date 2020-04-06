package com.martinmarinkovic.myapplication.roomdb

import androidx.room.*
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.io.Serializable
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList

@Entity
data class Note(
    @PrimaryKey var id: String = "",
    val title: String? = null,
    val note: String? = null,
    var images: ArrayList<String>? = null,
    var audioFiles: ArrayList<String>? = null,
    var date: Long? = null

): Serializable

class Converters {
    @TypeConverter
    fun fromString(value: String?): ArrayList<String> {
        val listType: Type = object : TypeToken<ArrayList<String?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<String?>?): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}

class TimestampConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}