package com.sergiocruz.MatematicaPro.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.gson.annotations.SerializedName

@Entity(primaryKeys = ["primary_key", "operation"])
data class HistoryDataClass(

        @SerializedName("primaryKey")
        @ColumnInfo(name = "primary_key")
        val primaryKey: String,

        @SerializedName("operation")
        @ColumnInfo(name = "operation")
        val operation: String,

        @SerializedName("content")
        @ColumnInfo(name = "content")
        val content: String,

        @SerializedName("persist")
        @ColumnInfo(name = "persist")
        val persist: Boolean

)

