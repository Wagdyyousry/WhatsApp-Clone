package com.wagdybuild.whatsapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.wagdybuild.whatsapp.models.User
import com.wagdybuild.whatsapp.R
import com.wagdybuild.whatsapp.ui.ChattingActivity
import de.hdodenhof.circleimageview.CircleImageView

class FragmentChatAdapter(val context: Context) :
    RecyclerView.Adapter<FragmentChatAdapter.ViewHolder>() {
    private var usersList: ArrayList<User>?=null
    private lateinit var db:FirebaseDatabase
    private lateinit var mAuth:FirebaseAuth


    fun setUserList(usersList: ArrayList<User>){
        this.usersList = usersList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val userView: View =LayoutInflater.from(parent.context).inflate(R.layout.user_view, parent, false)
        return ViewHolder(userView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userData = usersList!![position]

        db = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        val my_room_chat = mAuth.currentUser!!.uid + userData.id

        holder.userName.text = userData.name
        if (userData.profileImageUri != null) {
            Glide.with(context.applicationContext).load(userData.profileImageUri)
                .placeholder(R.drawable.ic_person)
                .into(holder.userImage);
        }



        //Getting last message
        gettingLastMessage(userData,holder)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChattingActivity::class.java)
            intent.putExtra("receiver_id", userData.id)
            intent.putExtra("receiver_name", userData.name)
            intent.putExtra("receiver_email", userData.email)
            intent.putExtra("receiver_profile_image", userData.profileImageUri)
            intent.putExtra("receiver_password", userData.password)
            intent.putExtra("receiver_last_message", userData.last_message)
            context.startActivity(intent)
        }

    }

    private fun gettingLastMessage(userData: User, holder: ViewHolder) {
        db.reference.child("Chats").child(mAuth.currentUser!!.uid)
            .child(userData.id)
            .orderByChild("time")
            .limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (dataSnapshot in snapshot.children) {
                        holder.lastMessage.text = dataSnapshot.child("message").value.toString()
                    }

                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun getItemCount(): Int {
        return if(usersList==null){
            0
        }
        else{
            return usersList!!.size

        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userImage: CircleImageView = view.findViewById(R.id.user_view_image)
        val userName: TextView = view.findViewById(R.id.user_name)
        val lastMessage: TextView = view.findViewById(R.id.last_massage)

    }
}