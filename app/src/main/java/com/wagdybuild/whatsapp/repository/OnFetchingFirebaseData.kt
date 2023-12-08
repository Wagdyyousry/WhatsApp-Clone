package com.wagdybuild.whatsapp.repository

import com.wagdybuild.whatsapp.models.DBFriendMessage
import com.wagdybuild.whatsapp.models.DBGroupMessage
import com.wagdybuild.whatsapp.models.Group
import com.wagdybuild.whatsapp.models.Message
import com.wagdybuild.whatsapp.models.Status
import com.wagdybuild.whatsapp.models.User

interface OnFetchingFirebaseData {
    fun onSuccessGettingCurrentUserData(user: User)
    fun onSuccessGettingGroupList(groupList: List<Group>)
    fun onSuccessGettingUserList(userList: List<User>)
    fun onSuccessGettingCurrentUserStatus(userStatusList: ArrayList<Status>)
    fun onSuccessGettingOthersStatus(otherStatusList: ArrayList<ArrayList<Status>>)
    fun onSuccessGettingGroupMessages(groupMessagesList: List<DBGroupMessage>)
    fun onSuccessGettingChatMessages(friendsMessagesList: List<DBFriendMessage>)
}