package com.wagdybuild.whatsapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wagdybuild.whatsapp.models.DBFriendMessage
import com.wagdybuild.whatsapp.models.DBGroupMessage
import com.wagdybuild.whatsapp.models.Group
import com.wagdybuild.whatsapp.models.User

@Dao
interface RoomDAO {
    /** Users */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOneUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserList(list: List<User>)

    @Query("select * from users_table where id=:userId")
    suspend fun getCurrentUserData(userId: String): User

    @Query("select * from users_table where id!=:userId")
    suspend fun gettingUserList(userId: String): List<User>

    /** Groups */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOneGroup(group: Group)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupList(list: List<Group>)

    @Query("select * from groups_table where groupId=:groupId")
    suspend fun gettingOneGroup(groupId: String): Group

    @Query("select * from groups_table")
    suspend fun gettingGroupList(): List<Group>

    /** Friends Messages */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriendsMessage(message: DBFriendMessage)

    @Query("select * from friendChatTable where receiver_id=:receiverId and sender_id=:senderId order by time asc")
    suspend fun gettingFriendsMessageList(receiverId: String, senderId: String): List<DBFriendMessage>

    /** Groups Messages */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMessage(message: DBGroupMessage)

    @Query("select * from groupChatTable where receiver_id=:groupId")
    suspend fun gettingGroupMessageList(groupId: String): List<DBGroupMessage>

}