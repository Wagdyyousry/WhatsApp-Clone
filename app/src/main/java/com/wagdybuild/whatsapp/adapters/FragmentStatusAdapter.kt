package com.wagdybuild.whatsapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devlomi.circularstatusview.CircularStatusView
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.wagdybuild.whatsapp.R
import com.wagdybuild.whatsapp.models.Status
import com.wagdybuild.whatsapp.models.User
import com.wagdybuild.whatsapp.ui.ChattingActivity
import de.hdodenhof.circleimageview.CircleImageView
import omari.hamza.storyview.StoryView
import omari.hamza.storyview.callback.StoryClickListeners
import omari.hamza.storyview.model.MyStory
import java.util.*

class FragmentStatusAdapter(context: Context)
    : RecyclerView.Adapter<FragmentStatusAdapter.ViewHolder>() {
    private val context: Context
    private var statusList: ArrayList<ArrayList<Status>>
    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseDatabase? = null
    private lateinit var user: User
    private var dStorage: FirebaseStorage? = null

    init {
        this.context = context
        statusList = ArrayList<ArrayList<Status>>()
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        dStorage = FirebaseStorage.getInstance()
    }

    fun setStatusList(statusList: ArrayList<ArrayList<Status>>) {
        this.statusList = statusList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.status_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val user_status_list = statusList[position]

        //getting current user data
        gettingCurrentUserData(user_status_list,holder)

        //if list is empty
        if (user_status_list.size > 0) {
            val status :Status = user_status_list[user_status_list.size - 1]

            holder.user_status.setPortionsCount(user_status_list.size)

            val timeAgo = TimeAgo.using(status.time)
            holder.created_time.text = timeAgo

            Glide.with(context)
                .load(status.StatusImageUri)
                .placeholder(R.drawable.ic_person)
                .into(holder.user_image)
        }


    }

    private fun gettingCurrentUserData(user_status_list: ArrayList<Status>, holder: ViewHolder) {
        db!!.reference.child("Users").child(user_status_list[0].user_id)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)!!
                    holder.user_name.text = user.name
                    putData(user,holder,user_status_list)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun putData(user: User, holder: ViewHolder, user_status_list: ArrayList<Status>) {
        this.user = user

        //on status item click
        holder.itemView.setOnClickListener { showStory(user_status_list,user) }
        //Log.d("+++++++++==========>>>>>",this.user.name)
    }

    private fun showStory(userStatusList: ArrayList<Status>, user: User) {
        val myStories = ArrayList<MyStory>()
        for (status in userStatusList) {
            myStories.add(MyStory(status.StatusImageUri, Date(status.time)))
        }
        val timeAgo = TimeAgo.using(userStatusList[0].time)
        StoryView.Builder((context as AppCompatActivity).supportFragmentManager)
            .setStoriesList(myStories) // Required
            .setStoryDuration(6000) // Default is 2000 Millis (2 Seconds)
            .setTitleText("| ${user.name} |") // Default is Hidden
            .setSubtitleText(timeAgo) // Default is Hidden
            .setTitleLogoUrl(user.profileImageUri) // Default is Hidden
            .setStoryClickListeners(object : StoryClickListeners {
                override fun onDescriptionClickListener(position: Int) {
                    //your action
                }

                override fun onTitleIconClickListener(position: Int) {
                    val intent = Intent(context, ChattingActivity::class.java)
                    intent.putExtra("receiver_id", user.id)
                    intent.putExtra("receiver_name", user.name)
                    intent.putExtra("receiver_email", user.email)
                    intent.putExtra("receiver_profile_image", user.profileImageUri)
                    intent.putExtra("receiver_password", user.password)
                    intent.putExtra("receiver_last_message", user.last_message)
                    context.startActivity(intent)
                }

            }) // Optional Listeners
            .build() // Must be called before calling show method
            .show()
    }

    override fun getItemCount(): Int {
        return if (statusList == null) {
            0
        } else {
            statusList.size
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var user_name: TextView
        var created_time: TextView
        var user_image: CircleImageView
        var user_status: CircularStatusView

        init {
            user_name = itemView.findViewById(R.id.user_name_status_view)
            created_time = itemView.findViewById(R.id.created_time_status_view)
            user_image = itemView.findViewById(R.id.user_image_status_view)
            user_status = itemView.findViewById(R.id.user_status_status_view)
        }
    }

}