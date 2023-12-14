package com.wagdybuild.whatsapp.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.wagdybuild.whatsapp.models.DBFriendMessage
import com.wagdybuild.whatsapp.models.DBGroupMessage
import com.wagdybuild.whatsapp.models.Group
import com.wagdybuild.whatsapp.models.Status
import com.wagdybuild.whatsapp.models.User
import com.wagdybuild.whatsapp.repository.FirebaseDatabaseRepo
import com.wagdybuild.whatsapp.repository.OnFetchingFirebaseData
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
class FirebaseDatabaseViewModel(application: Application) : AndroidViewModel(application) {
    private var userListLiveData = MutableLiveData<List<User>>()
    private var currentUserDataLiveData = MutableLiveData<User>()
    private var groupListLiveData = MutableLiveData<List<Group>>()
    private var currentUserStatusLiveData = MutableLiveData<ArrayList<Status>>()
    private var othersStatusLiveData = MutableLiveData<ArrayList<ArrayList<Status>>>()
    private var chatMessagesListLiveData = MutableLiveData<List<DBFriendMessage>>()
    private var groupMessagesListLiveData = MutableLiveData<List<DBGroupMessage>>()
    private var repo: FirebaseDatabaseRepo? = null

    init {
        repo = FirebaseDatabaseRepo(application, object : OnFetchingFirebaseData {
            override fun onSuccessGettingCurrentUserData(user: User) {
                /*GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        currentUserDataLiveData.postValue(user)
                    }
                }*/
                currentUserDataLiveData.postValue(user)
            }
            override fun onSuccessGettingGroupList(groupList: List<Group>) {
               groupListLiveData.postValue(groupList)
            }
            override fun onSuccessGettingUserList(userList: List<User>) {
                userListLiveData.postValue(userList)
            }
            override fun onSuccessGettingCurrentUserStatus(userStatusList: ArrayList<Status>) {
                currentUserStatusLiveData.postValue(userStatusList)
            }
            override fun onSuccessGettingOthersStatus(otherStatusList: ArrayList<ArrayList<Status>>) {
                othersStatusLiveData.postValue(otherStatusList)
            }
            override fun onSuccessGettingGroupMessages(groupMessagesList: List<DBGroupMessage>) {
                groupMessagesListLiveData.postValue(groupMessagesList)

            }
            override fun onSuccessGettingChatMessages(friendsMessagesList: List<DBFriendMessage>) {
                chatMessagesListLiveData.postValue(friendsMessagesList)
            }
        })

        repo!!.gettingUserList()
        repo!!.gettingCurrentUserData()
        repo!!.gettingGroupList()
        repo!!.gettingCurrentUserStatus()
        repo!!.gettingOthersStatus()
    }

    //Getting all data  from repo as Live Data type
    fun getUserListLiveData(): MutableLiveData<List<User>> {
        return userListLiveData
    }

    fun getCurrentUserDataLiveData(): MutableLiveData<User> {
        return currentUserDataLiveData
    }

    fun getGroupListLiveData(): MutableLiveData<List<Group>> {
        return groupListLiveData
    }

    fun getCurrentUserStatusLiveData(): MutableLiveData<ArrayList<Status>> {
        return currentUserStatusLiveData
    }

    fun getOthersStatusLiveData(): MutableLiveData<ArrayList<ArrayList<Status>>> {
        return othersStatusLiveData
    }

    fun getChatMessagesListLiveData(): MutableLiveData<List<DBFriendMessage>> {
        return chatMessagesListLiveData
    }

    fun getGroupMessagesListLiveData(): MutableLiveData<List<DBGroupMessage>> {
        return groupMessagesListLiveData
    }

    //parsing attrs to repository to get data from Room Database firebase

    fun gettingChatMessagesList(receiverId: String) {
        repo!!.gettingChatMessages(receiverId)
    }

    fun gettingGroupMessagesList(groupId: String) {
        repo!!.gettingGroupMessages(groupId)
    }


}