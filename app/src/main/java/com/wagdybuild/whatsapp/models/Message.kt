package com.wagdybuild.whatsapp.models

import androidx.room.Entity

data class Message(
    var message: String = "",
    var message_id: String = "",
    var sender_id: String = "",
    var receiver_id: String = "",
    var time: Long = 0,
    var imageUri: String? = null,
    var sender_imageUri: String? = null,
    var sender_name: String = "",
    var message_type: String = "",
)
