package com.wagdybuild.whatsapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.wagdybuild.whatsapp.R
import com.wagdybuild.whatsapp.models.User
import com.wagdybuild.whatsapp.ui.CreateGroupListener
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.collections.ArrayList

class CreateGroupAdapter(
    context: Context,
    groupListener: CreateGroupListener
) :
    RecyclerView.Adapter<CreateGroupAdapter.ViewHolder>() {
    private var groupListener: CreateGroupListener
    private val context: Context
    private var usersList: ArrayList<User>?=null
    private val selectedUsersList: ArrayList<String>
    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseDatabase? = null
    private var dbStorage: FirebaseStorage? = null
    private var check: Boolean = false

    init {
        this.context = context
        this.groupListener = groupListener

        usersList = ArrayList()
        selectedUsersList = ArrayList()
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        dbStorage = FirebaseStorage.getInstance()
    }
    fun setUserList(usersList: ArrayList<User>){
        this.usersList = usersList
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.select_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val user = usersList!![position]

        if (user.profileImageUri != null) {
            Glide.with(context.applicationContext).load(user.profileImageUri)
                .placeholder(R.drawable.ic_person)
                .into(holder.user_image)
        }
        holder.user_name.text = user.name

        holder.check_box.setOnClickListener {
            if (check) {
                holder.check_box.isChecked = false
                selectedUsersList.remove(user.id)
                check = false
            } else {
                holder.check_box.isChecked = true
                selectedUsersList.add(user.id)
                check = true
            }
            groupListener.onSelectItems(selectedUsersList)
        }

    }

    override fun getItemCount(): Int {
        if(usersList==null){
           return 0
        }
        else{
           return usersList!!.size
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var user_name: TextView
        var check_box: CheckBox
        var user_image: CircleImageView

        init {
            user_name = itemView.findViewById(R.id.select_item_user_name)
            check_box = itemView.findViewById(R.id.select_item_check)
            user_image = itemView.findViewById(R.id.select_item_image)
        }
    }

}