package com.wagdybuild.whatsapp.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.wagdybuild.whatsapp.R
import com.wagdybuild.whatsapp.adapters.FragmentStatusAdapter
import com.wagdybuild.whatsapp.databinding.FragmentStatusBinding
import com.wagdybuild.whatsapp.models.Status
import com.wagdybuild.whatsapp.models.User
import com.wagdybuild.whatsapp.ui.AddStatusImageActivity
import com.wagdybuild.whatsapp.ui.MainActivity
import com.wagdybuild.whatsapp.viewModel.FirebaseDatabaseViewModel
import omari.hamza.storyview.StoryView
import omari.hamza.storyview.callback.StoryClickListeners
import omari.hamza.storyview.model.MyStory
import java.text.SimpleDateFormat
import java.util.*


class StatusFragment : Fragment() {
    private lateinit var binding: FragmentStatusBinding
    private lateinit var firebaseViewModel: FirebaseDatabaseViewModel
    private lateinit var otherStatusList: ArrayList<ArrayList<Status>>
    private lateinit var userStatusList: ArrayList<Status>
    private lateinit var adapter: FragmentStatusAdapter
    private lateinit var rvStatusFragment: RecyclerView
    private var dbStorage: FirebaseStorage? = null
    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseDatabase? = null
    private var user: User? = null
    private var cActivityResultLauncher: ActivityResultLauncher<String>? = null

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatusBinding.inflate(inflater, container, false)

        //initialize all variables
        initialize()

        gettingCurrentUserData()

        binding.fabImageStatus.setOnClickListener {
            cActivityResultLauncher!!.launch("image/*")
        }

        binding.btnAddStatus.setOnClickListener {
            cActivityResultLauncher!!.launch("image/*")
        }

        //getting current user Stories
        gettingCurrentUserStatus()

        //getting others users stories
        gettingOthersStatus()

        return binding.root
    }

    private fun initialize() {
        //Firebase references
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        dbStorage = FirebaseStorage.getInstance()

        otherStatusList = ArrayList<ArrayList<Status>>()
        userStatusList = ArrayList()

        adapter = FragmentStatusAdapter(requireContext())
        rvStatusFragment = binding.rvStatusFragment
        rvStatusFragment.setHasFixedSize(true)
        rvStatusFragment.layoutManager = LinearLayoutManager(context)
        rvStatusFragment.adapter = adapter


        //image lips
        cActivityResultLauncher = registerForActivityResult<String, Uri>(
            ActivityResultContracts.GetContent()
        ) {
            if(it!=null){
                val intent = Intent(context, AddStatusImageActivity::class.java)
                intent.putExtra("image", it.toString())
                context?.startActivity(intent)
            }

        }

        //Others Status view model
        firebaseViewModel = (activity as MainActivity).firebaseViewModel
    }

    private fun gettingCurrentUserData() {
        firebaseViewModel.getCurrentUserDataLiveData().observe(viewLifecycleOwner) {
            if (it != null) {
                user = it
                binding.userName.text = it.name
                if (it.profileImageUri != null) {
                    Glide.with(requireContext()).load(it.profileImageUri)
                        .placeholder(R.drawable.ic_person).into(binding.civ)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged", "SimpleDateFormat")
    private fun gettingCurrentUserStatus() {

        userStatusList.clear()
        firebaseViewModel.getCurrentUserStatusLiveData().observe(viewLifecycleOwner) {
            if (it != null) {
                userStatusList = it

                if (it.size > 0) {
                    binding.csv.setPortionsCount(it.size)
                    binding.btnAddStatus.visibility = View.GONE

                    val status = it[it.size - 1]
                    val sdf = SimpleDateFormat("hh:mm a")

                    Glide.with(requireContext()).load(status.StatusImageUri)
                        .placeholder(R.drawable.ic_person).into(binding.civ)

                    val timeAgo = TimeAgo.using(status.time)
                    binding.createdDate.text = timeAgo

                } else {
                    binding.btnAddStatus.visibility = View.VISIBLE
                    binding.csv.setPortionsCount(0)
                }
            }
        }

        binding.myStatusLayout.setOnClickListener {
            if (userStatusList.size > 0) {
                val myStories = ArrayList<MyStory>()

                for (status in userStatusList) {
                    myStories.add(
                        MyStory(
                            status.StatusImageUri,
                            Date(status.time)
                        )
                    )
                }

                val timeAgo = TimeAgo.using(userStatusList[0].time)
                StoryView.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setStoriesList(myStories) // Required
                    .setStoryDuration(6000) // Default is 2000 Millis (2 Seconds)
                    .setTitleText("|${user!!.name}|") // Default is Hidden
                    .setSubtitleText(timeAgo) // Default is Hidden
                    .setTitleLogoUrl(user!!.profileImageUri) // Default is Hidden
                    .setStoryClickListeners(object : StoryClickListeners {
                        override fun onDescriptionClickListener(position: Int) {
                            //your action
                        }

                        override fun onTitleIconClickListener(position: Int) {
                        }
                    }) // Optional Listeners
                    .build() // Must be called before calling show method
                    .show()
            }

        }

        /*db!!.reference.child("Status").child(mAuth!!.currentUser!!.uid)
          .addValueEventListener(object : ValueEventListener {
              override fun onDataChange(snapshot: DataSnapshot) {
                  userStatusList.clear()
                  for (dataSnapshot in snapshot.children) {
                      val userData = dataSnapshot.getValue(Status::class.java)
                      userStatusList.add(userData!!)
                  }
                  adapter.notifyDataSetChanged()
                  if (userStatusList.size > 0) {
                      binding.csv.setPortionsCount(userStatusList.size)
                      binding.btnAddStatus.visibility = View.GONE

                      val status = userStatusList[userStatusList.size - 1]
                      val sdf = SimpleDateFormat("hh:mm a")

                      val time_ago = TimeAgo.using(status.time)
                      binding.createdDate.text = time_ago

                  } else {
                      binding.btnAddStatus.visibility = View.VISIBLE
                      binding.csv.setPortionsCount(0)
                  }
              }

              override fun onCancelled(error: DatabaseError) {}
          })*/

        /* if(userStatusList.size > 0){
             val status = userStatusList[userStatusList.size-1]
             Glide.with(requireContext())
                 .load(status.StatusImageUri)
                 .placeholder(R.drawable.ic_person)
                 .into(binding.civ)
         }*/
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun gettingOthersStatus() {
        otherStatusList.clear()
        firebaseViewModel.getOthersStatusLiveData().observe(viewLifecycleOwner) {
            if (it != null && it.size > 0) {
                adapter.setStatusList(it)
                adapter.notifyDataSetChanged()
            }
        }
       /* db!!.reference.child("Status").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                otherStatusList.clear()
                for (dataSnapshot1 in snapshot.children) {
                    val each_user_status = ArrayList<Status>()
                    for (dataSnapshot2 in dataSnapshot1.children) {
                        val status = dataSnapshot2.getValue(Status::class.java)
                        each_user_status.add(status!!)
                    }
                    if (each_user_status[0].user_id != mAuth!!.currentUser!!.uid) {
                        otherStatusList.add(each_user_status)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })*/
    }


}