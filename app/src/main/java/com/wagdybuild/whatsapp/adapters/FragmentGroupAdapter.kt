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
import com.google.firebase.storage.FirebaseStorage
import com.wagdybuild.whatsapp.R
import com.wagdybuild.whatsapp.models.Group
import com.wagdybuild.whatsapp.models.User
import com.wagdybuild.whatsapp.ui.GroupChattingActivity
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.collections.ArrayList

class FragmentGroupAdapter(context: Context) :
    RecyclerView.Adapter<FragmentGroupAdapter.ViewHolder>() {
    //private var groupListener: CreateGroupListener
    private val context: Context
    private var groupList = ArrayList<Group>()
    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseDatabase? = null
    private var dbStorage: FirebaseStorage? = null

    init {
        this.context = context
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        dbStorage = FirebaseStorage.getInstance()
    }

    fun setGroupList(groupList: ArrayList<Group>) {
        this.groupList = groupList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = groupList[position]

        if (group.group_imageUri != null) {
            Glide.with(context.applicationContext).load(group.group_imageUri)
                .placeholder(R.drawable.ic_group)
                .into(holder.group_image)
        }

        db!!.reference.child("Groups_Chat").child(group.groupId)
            .orderByChild("time")
            .limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (dataSnapshot in snapshot.children) {
                        holder.last_messsage.text = dataSnapshot.child("message").value.toString()
                    }

                }

                override fun onCancelled(error: DatabaseError) {}


            })

        holder.group_name.text = group.groupName

        holder.itemView.setOnClickListener {
            val intent = Intent(context, GroupChattingActivity::class.java)
            intent.putExtra("group_id", group.groupId)
            intent.putExtra("group_name", group.groupName)
            intent.putExtra("group_imageUri", group.group_imageUri)
            intent.putStringArrayListExtra("group_members", group.groupMembers)
            intent.putExtra("group_creater_id", group.creatorId)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        if(groupList == null){
            return 0
        }
        else{
            return groupList.size
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var group_name: TextView
        var last_messsage: TextView
        var group_image: CircleImageView

        init {
            group_name = itemView.findViewById(R.id.user_name)
            last_messsage = itemView.findViewById(R.id.last_massage)
            group_image = itemView.findViewById(R.id.user_view_image)
        }
    }

}