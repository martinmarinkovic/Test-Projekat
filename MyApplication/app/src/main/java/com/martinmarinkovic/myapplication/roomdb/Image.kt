package com.martinmarinkovic.myapplication.roomdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity (tableName = "image")
data class Image(

    @PrimaryKey(autoGenerate = true)

    var id: Int = 1,

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var data: ByteArray? = null
){

}