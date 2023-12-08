package com.wagdybuild.whatsapp.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.wagdybuild.whatsapp.adapters.FragmentGroupAdapter
import com.wagdybuild.whatsapp.databinding.FragmentGroupsBinding
import com.wagdybuild.whatsapp.models.Group
import com.wagdybuild.whatsapp.models.User
import com.wagdybuild.whatsapp.ui.MainActivity
import com.wagdybuild.whatsapp.viewModel.FirebaseDatabaseViewModel
import kotlin.collections.ArrayList

class GroupsFragment : Fragment() {
    private lateinit var binding: FragmentGroupsBinding
    private lateinit var firebaseViewModel: FirebaseDatabaseViewModel
    private lateinit var groupsList: ArrayList<Group>
    private lateinit var joined_groups: ArrayList<String>
    private lateinit var all_groups: ArrayList<String>
    private lateinit var group_members: ArrayList<User>
    private lateinit var adapter: FragmentGroupAdapter
    private lateinit var rv_groups_fragment: RecyclerView
    private var dbStorage: FirebaseStorage? = null
    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseDatabase? = null
    private var user: User? = null
    private var cActivityResultLauncher: ActivityResultLauncher<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGroupsBinding.inflate(inflater, container, false)

        //initialize all variables
        initialize()

        gettingJoinedGroups()

        return binding.root
    }

    private fun initialize() {
        //Firebase references
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        dbStorage = FirebaseStorage.getInstance()
        groupsList = ArrayList()

        adapter = FragmentGroupAdapter(requireContext())
        rv_groups_fragment = binding.rvGroupsFragment
        rv_groups_fragment.setHasFixedSize(true)
        rv_groups_fragment.layoutManager = LinearLayoutManager(context)
        rv_groups_fragment.adapter = adapter

        //Groups view model
        firebaseViewModel = (activity as MainActivity).firebaseViewModel

    }
    @SuppressLint("NotifyDataSetChanged")
    private fun gettingJoinedGroups() {
        groupsList.clear()
        firebaseViewModel.getGroupListLiveData().observe(viewLifecycleOwner) {
            if (it != null && it.isNotEmpty()) {
                groupsList = it as ArrayList<Group>
                binding.pbGroupsFragment.visibility = View.GONE
                adapter.setGroupList(groupsList)
                adapter.notifyDataSetChanged()
            }
        }
    }

}