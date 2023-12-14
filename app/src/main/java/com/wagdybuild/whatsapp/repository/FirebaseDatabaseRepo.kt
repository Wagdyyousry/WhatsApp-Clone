package com.wagdybuild.whatsapp.repository

import android.annotation.SuppressLint
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.wagdybuild.whatsapp.db.RoomDAO
import com.wagdybuild.whatsapp.db.RoomDatabase
import com.wagdybuild.whatsapp.models.DBFriendMessage
import com.wagdybuild.whatsapp.models.DBGroupMessage
import com.wagdybuild.whatsapp.models.Group
import com.wagdybuild.whatsapp.models.Status
import com.wagdybuild.whatsapp.models.User
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
class FirebaseDatabaseRepo(context: Context, onGettingAllDataInterface: OnFetchingFirebaseData) {
    private var onGettingAllDataInterface: OnFetchingFirebaseData? = null
    private var mAuth: FirebaseAuth? = FirebaseAuth.getInstance()
    private var db: FirebaseDatabase? = FirebaseDatabase.getInstance()
    private var roomDAO: RoomDAO

    init {
        this.onGettingAllDataInterface = onGettingAllDataInterface
        roomDAO = RoomDatabase.getINSTANCE(context).roomDAO()
    }

    fun gettingCurrentUserData() {
        db!!.reference.child("Users").child(mAuth!!.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userData = snapshot.getValue(User::class.java)!!
                    GlobalScope.launch {
                        roomDAO.insertOneUser(userData)
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        var user: User
        GlobalScope.launch {
            user = roomDAO.getCurrentUserData(mAuth!!.currentUser!!.uid)
            if (user != null) {
                onGettingAllDataInterface!!.onSuccessGettingCurrentUserData(user)
            }
        }


    }

    fun gettingUserList() {
        db!!.reference.child("Users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val userData = dataSnapshot.getValue(User::class.java)!!
                    GlobalScope.launch {
                        roomDAO.insertOneUser(userData)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                //onGettingUsersInterface!!.onError(error)
            }
        })
        var list: List<User>
        GlobalScope.launch {
            list = roomDAO.gettingUserList(mAuth!!.currentUser!!.uid)
            if (list.isNotEmpty()) {
                onGettingAllDataInterface!!.onSuccessGettingUserList(list)
            }
        }

    }

    fun gettingGroupList() {
        db!!.getReference("Groups").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val groupMembersList = dataSnapshot.child("groupMembers").value as ArrayList<String>
                        for (member in groupMembersList) {
                            if (member == mAuth!!.currentUser!!.uid) {
                                val group = dataSnapshot.getValue(Group::class.java)!!
                                GlobalScope.launch {
                                    roomDAO.insertOneGroup(group)
                                }
                                continue
                            }
                        }
                    }

                }

            }

            override fun onCancelled(error: DatabaseError) {}
        })

        var list: List<Group>?
        GlobalScope.launch {
            list = roomDAO.gettingGroupList()
            if (list!!.isNotEmpty()) {
                onGettingAllDataInterface!!.onSuccessGettingGroupList(list!!)
            }
        }

    }

    fun gettingChatMessages(receiverId: String) {
        var check = 0
        db!!.reference.child("Chats")
            .child(mAuth!!.currentUser!!.uid)
            .child(receiverId).addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (dataSnapshot in snapshot.children) {
                        val messageModel = dataSnapshot.getValue(DBFriendMessage::class.java)
                        messageModel!!.message_id = dataSnapshot.key.toString()
                        GlobalScope.launch {
                            roomDAO.insertFriendsMessage(messageModel)
                        }
                    }
                    var list: List<DBFriendMessage>?
                    GlobalScope.launch {
                        delay(380)
                        list =
                            roomDAO.gettingFriendsMessageList(receiverId, mAuth!!.currentUser!!.uid)
                        if (list!!.isNotEmpty()) {
                            check++
                            onGettingAllDataInterface!!.onSuccessGettingChatMessages(list!!)
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {}
            })


        var list: List<DBFriendMessage>? = null
        GlobalScope.launch {
            list = roomDAO.gettingFriendsMessageList(receiverId, mAuth!!.currentUser!!.uid)
            if (check == 0 && list!!.isNotEmpty()) {
                onGettingAllDataInterface!!.onSuccessGettingChatMessages(list!!)
            }
        }


    }

    fun gettingGroupMessages(groupId: String) {
        var check = 0
        db!!.reference.child("Groups_Chat")
            .child(groupId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (dataSnapshot in snapshot.children) {
                        val messageModel = dataSnapshot.getValue(DBGroupMessage::class.java)!!
                        messageModel.message_id = dataSnapshot.key.toString()
                        GlobalScope.launch {
                            roomDAO.insertGroupMessage(messageModel)
                        }
                    }
                    var list: List<DBGroupMessage>?
                    GlobalScope.launch {
                        delay(300)
                        list = roomDAO.gettingGroupMessageList(groupId)
                        if (list!!.isNotEmpty()) {
                            check++
                            onGettingAllDataInterface!!.onSuccessGettingGroupMessages(list!!)
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    //onGettingGroupMessagesInterface!!.onError(error)
                }
            })

        var list: List<DBGroupMessage>? = null
        GlobalScope.launch {
            list = roomDAO.gettingGroupMessageList(groupId)
            if (check == 0 && list!!.isNotEmpty()) {
                onGettingAllDataInterface!!.onSuccessGettingGroupMessages(list!!)
            }
        }

    }

    fun gettingCurrentUserStatus() {
        val statusList = ArrayList<Status>()

        db!!.reference.child("Status").child(mAuth!!.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    statusList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val status = dataSnapshot.getValue(Status::class.java)!!
                        status.status_id = dataSnapshot.key.toString()
                        statusList.add(status)
                    }
                    onGettingAllDataInterface!!.onSuccessGettingCurrentUserStatus(statusList)

                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    fun gettingOthersStatus() {
        val statusList = ArrayList<ArrayList<Status>>()

        db!!.reference.child("Status").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                statusList.clear()
                for (dataSnapshot1 in snapshot.children) {
                    val eachUserStatus = ArrayList<Status>()

                    for (dataSnapshot2 in dataSnapshot1.children) {
                        val status = dataSnapshot2.getValue(Status::class.java)!!
                        status.status_id = dataSnapshot2.key.toString()

                        eachUserStatus.add(status)
                    }
                    if (eachUserStatus[0].user_id != mAuth!!.currentUser!!.uid) {
                        statusList.add(eachUserStatus)
                    }
                }
                onGettingAllDataInterface!!.onSuccessGettingOthersStatus(statusList)
            }

            override fun onCancelled(error: DatabaseError) {
                //onGettingStatusInterface!!.onError(error)
            }
        })

    }

}