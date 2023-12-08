package com.wagdybuild.whatsapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.wagdybuild.whatsapp.db.GroupConverter

@Entity(tableName ="groups_table")
@TypeConverters(GroupConverter::class)
data class Group(
    var groupName: String = "",
    @PrimaryKey
    var groupId: String = "",
    var creatorId: String = "",
    var group_imageUri: String? = null,
    val groupMembers: ArrayList<String> = ArrayList()
)