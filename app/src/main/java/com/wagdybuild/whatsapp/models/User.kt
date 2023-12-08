package com.wagdybuild.whatsapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName ="users_table")
data class User(
    var name: String = "",
    var email: String = "",
    var password: String = "",
    @PrimaryKey
    var id: String = "",
    var last_message: String = "",
    var bio: String = "",
    var profileImageUri: String? = null
)