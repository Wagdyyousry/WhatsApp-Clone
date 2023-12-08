package com.wagdybuild.whatsapp.models

import androidx.room.Entity

data class Status(
    var status_id: String = "",
    var user_id: String = "",
    var time: Long = 0,
    var type:String="",
    var caption:String="",
    var StatusImageUri: String? = null,
    var StatusVideoUri: String? = null,
    var user_name: String = ""

)
