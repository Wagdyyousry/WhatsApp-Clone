package com.wagdybuild.whatsapp.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.wagdybuild.whatsapp.R
import com.wagdybuild.whatsapp.adapters.CreateGroupAdapter
import com.wagdybuild.whatsapp.databinding.ActivityCreateGroupBinding
import com.wagdybuild.whatsapp.databinding.CreateGroupDialogBinding
import com.wagdybuild.whatsapp.models.Group
import com.wagdybuild.whatsapp.models.User
import com.wagdybuild.whatsapp.viewModel.FirebaseDatabaseViewModel
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class CreateGroupActivity : AppCompatActivity(), CreateGroupListener {
    private var cActivityResultLauncher: ActivityResultLauncher<String>? = null
    private lateinit var binding: ActivityCreateGroupBinding
    private lateinit var alertDialogBinding: CreateGroupDialogBinding
    private lateinit var firebaseViewModel: FirebaseDatabaseViewModel

    /** Recycle View */
    private lateinit var adapter: CreateGroupAdapter
    private lateinit var rv_group: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var selectedUsers: ArrayList<String>
    private var result_uri_base: Uri? = null

    /** Firebase */
    private var dbStorage: FirebaseStorage? = null
    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnSubmit.visibility = View.GONE

        initialize()

        gettingUsers()

        binding.btnBack.setOnClickListener { onBackPressed() }

        /** Custom dialog create new group */
        customDialog()

    }

    private fun initialize() {
        //Custom status bar
        window.statusBarColor = ContextCompat.getColor(this, R.color.status_background)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)

        //Firebase references
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        dbStorage = FirebaseStorage.getInstance()

        selectedUsers = ArrayList()
        userList = ArrayList()

        //image lips
        cActivityResultLauncher = registerForActivityResult<String, Uri>(
            ActivityResultContracts.GetContent()
        ) {
            if (it != null) {
                startCrop(it)
            }
        }

        //user data view model
        firebaseViewModel = ViewModelProvider(this)[FirebaseDatabaseViewModel(application)::class.java]

        adapter = CreateGroupAdapter(this, this)
        rv_group = binding.rvCreateGroup
        rv_group.setHasFixedSize(true)
        rv_group.layoutManager = LinearLayoutManager(this)
        rv_group.adapter = adapter
    }

    @SuppressLint("ResourceAsColor")
    private fun customDialog() {
        /** Custom dialog create new group */
        alertDialogBinding = CreateGroupDialogBinding.inflate(LayoutInflater.from(this))
        val builder = AlertDialog.Builder(this).setView(alertDialogBinding.root).create()
        builder.window!!.setBackgroundDrawable(ColorDrawable(android.R.color.transparent))
        binding.btnSubmit.setOnClickListener { builder.show() }
        builder.setCanceledOnTouchOutside(false)

        alertDialogBinding.dialogBtnCancel.setOnClickListener { builder.dismiss() }

        alertDialogBinding.dialogBtnImage.setOnClickListener { cActivityResultLauncher!!.launch("image/*") }

        alertDialogBinding.dialogBtnCreate.setOnClickListener {
            val group_name = alertDialogBinding.dialogGroupName.text.toString()
            if (group_name.isEmpty()) {
                alertDialogBinding.dialogGroupName.error = "You have to write Group name"
                return@setOnClickListener
            }
            if (selectedUsers.size <= 0 || selectedUsers.isEmpty()) {
                Toast.makeText(
                    this@CreateGroupActivity,
                    "You didn`t choose members",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            } else {
                builder.dismiss()
                createGroup(selectedUsers, group_name)
            }

        }

    }

    private fun createGroup(selectedUsers: ArrayList<String>, group_name: String) {
        selectedUsers.add(mAuth!!.currentUser!!.uid)
        val group_id = Date().time.toString()
        val group = Group(
            group_name,
            group_id,
            mAuth!!.currentUser!!.uid,
            result_uri_base.toString(),
            selectedUsers
        )

        db!!.reference.child("Groups").child(group_id).setValue(group).addOnSuccessListener {
            Toast.makeText(
                this@CreateGroupActivity,
                "Group Created Successfully",
                Toast.LENGTH_LONG
            ).show()
            if (result_uri_base != null) {
                storeGroupImage(result_uri_base!!, group_id)
            }
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun gettingUsers() {
        userList.clear()
        firebaseViewModel.getUserListLiveData().observe(this) {
            if (it != null) {
                userList = it as ArrayList<User>
                adapter.setUserList(userList)
                adapter.notifyDataSetChanged()
                binding.pbCreateGroup.visibility = View.GONE
            }
        }
    }

    override fun onSelectItems(usersList: ArrayList<String>?) {

        if (usersList != null && usersList.size > 0) {
            selectedUsers = usersList
            binding.btnSubmit.visibility = View.VISIBLE
        } else {
            binding.btnSubmit.visibility = View.GONE
        }


    }

    private fun startCrop(crop_uri: Uri) {
        val time = Date().time.toString()
        val uCrop = UCrop.of(crop_uri, Uri.fromFile(File(cacheDir, "$time.jpg")))
        uCrop.withAspectRatio(5f, 5f)
        uCrop.withMaxResultSize(700, 700)
        uCrop.withOptions(getCropOption()!!)
        uCrop.start(this)
    }

    private fun getCropOption(): UCrop.Options {
        val options = UCrop.Options()
        //options.setCompressionQuality(70)
        options.setHideBottomControls(false)
        options.setFreeStyleCropEnabled(true)
        options.setStatusBarColor(getColor(R.color.black))
        options.setStatusBarColor(getColor(R.color.black))
        options.setToolbarTitle("Move Photo to crop")
        return options
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            var result_uri_crop: Uri? = null
            if (data != null) {
                result_uri_crop = UCrop.getOutput(data)
            }
            if (result_uri_crop != null) {
                result_uri_base = result_uri_crop
                alertDialogBinding.dialogGroupImage.setImageURI(result_uri_base)
            }
        }
    }

    private fun storeGroupImage(imageUri: Uri, group_id: String) {
        val reference = dbStorage!!.reference.child("Group_images").child(group_id)
        reference.putFile(imageUri).addOnSuccessListener {
            reference.downloadUrl.addOnSuccessListener {
                db!!.reference.child("Groups").child(group_id)
                    .child("group_imageUri")
                    .setValue(imageUri.toString())
            }
        }

    }
}