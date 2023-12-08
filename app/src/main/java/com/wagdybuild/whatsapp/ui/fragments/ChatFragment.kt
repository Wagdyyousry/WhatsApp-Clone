package com.wagdybuild.whatsapp.ui.fragments

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.wagdybuild.whatsapp.adapters.FragmentChatAdapter
import com.wagdybuild.whatsapp.models.User
import com.wagdybuild.whatsapp.databinding.FragmentChatBinding
import com.wagdybuild.whatsapp.ui.MainActivity
import com.wagdybuild.whatsapp.viewModel.FirebaseDatabaseViewModel


class ChatFragment : Fragment() {
    private lateinit var firebaseViewModel: FirebaseDatabaseViewModel
    private var db: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null
    private lateinit var binding: FragmentChatBinding
    private lateinit var adapter: FragmentChatAdapter
    private lateinit var rv_chats_fragment: RecyclerView
    private var userList = ArrayList<User>()

    @SuppressLint("RemoteViewLayout")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        db = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        userList = ArrayList()

        adapter = FragmentChatAdapter(requireContext())
        rv_chats_fragment = binding.rvChatsFragment
        rv_chats_fragment.setHasFixedSize(true)
        rv_chats_fragment.layoutManager = LinearLayoutManager(context)
        rv_chats_fragment.adapter = adapter


        //users view model
        firebaseViewModel = (activity as MainActivity).firebaseViewModel

        gettingUsers()

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun gettingUsers() {
        userList.clear()
        firebaseViewModel.getUserListLiveData().observe(viewLifecycleOwner) {
            if (it != null && it.isNotEmpty()) {
                binding.pbChatFragment.visibility = View.GONE
                userList = it as ArrayList<User>
                adapter.setUserList(userList)
                adapter.notifyDataSetChanged()
            }
        }
    }


}
