package com.wagdybuild.whatsapp.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GroupConverter {
    @TypeConverter
    fun fromList(value : ArrayList<String>):String {
        var s = ""
        if(value.isNotEmpty()){
            s= Json.encodeToString(value)
            //s= Gson().toJson(value)
        }
        return s
    }

    @TypeConverter
    fun toList(value: String):ArrayList<String> {
        var list = ArrayList<String>()
        if(value.isNotEmpty()){
            list = Json.decodeFromString<ArrayList<String>>(value)
            //list = Gson().fromJson(value, ArrayList<String>::class.java)
        }
        return list

    }

    /*@TypeConverter
    fun listToJsonString(value: List<Status>?): String = Gson().toJson(value)

    @TypeConverter
    fun jsonStringToList(value: String) = Gson().fromJson(value, Array<Status>::class.java).toList()

    @TypeConverter
    fun listToJson(value: List<Status>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String) = Gson().fromJson(value, Array<Status>::class.java).toList()*/
}