package com.wagdybuild.whatsapp.db

import androidx.room.TypeConverter
import com.wagdybuild.whatsapp.models.Message
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
class MessageConverter {
    @TypeConverter
    fun fromList(value : ArrayList<Message>):String {
        var s = ""
        if(value.isNotEmpty()){
            s= Json.encodeToString(value)
        }
        return s
    }

    @TypeConverter
    fun toList(value: String):ArrayList<Message> {
        var list = ArrayList<Message>()
        if(value.isNotEmpty()){
            list = Json.decodeFromString<ArrayList<Message>>(value)
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