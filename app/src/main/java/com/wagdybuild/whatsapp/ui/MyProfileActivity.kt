package com.wagdybuild.whatsapp.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.wagdybuild.whatsapp.models.User
import com.wagdybuild.whatsapp.R
import com.wagdybuild.whatsapp.databinding.ActivityMyProfileBinding
import com.wagdybuild.whatsapp.viewModel.FirebaseDatabaseViewModel
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.*


class MyProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyProfileBinding
    private lateinit var firebaseViewModel:FirebaseDatabaseViewModel
    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseDatabase? = null
    private var dbStorage: FirebaseStorage? = null
    private var current_name: String? = null
    private var current_bio: String? = null
    private var result_uri: Uri? = null
    private lateinit var user :User
    private var cActivityResultLauncher: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()

        binding.profileBtnBack.setOnClickListener { onBackPressed() }

        //Getting  Current user data
        gettingCurrentUserData()

        //closing fields
        closeFields()

        binding.profileBtnEdit.setOnClickListener {
            openFields()
        }

    }
    private fun initialize() {
        //Custom status bar
        window.statusBarColor = ContextCompat.getColor(this,R.color.status_background)
        window.navigationBarColor = ContextCompat.getColor(this,R.color.black)

        //FireBase
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        dbStorage = FirebaseStorage.getInstance()
        //image lips
        cActivityResultLauncher = registerForActivityResult<String, Uri>(
            ActivityResultContracts.GetContent()
        ) {
            startCrop(it)
        }

        //user data view model
        firebaseViewModel = ViewModelProvider(this)[FirebaseDatabaseViewModel(application)::class.java]
    }

    private fun gettingCurrentUserData() {
        firebaseViewModel.getCurrentUserDataLiveData().observe(this) {
            if (it != null) {
                user = it
                if (it.profileImageUri != null) {
                    Glide.with(this).load(user.profileImageUri)
                        .placeholder(R.drawable.ic_person)
                        .into(binding.profileImage);
                }
                binding.profileEtName.setText(it.name)
                binding.profileEtBio.setText(it.bio)
                this.current_bio = it.bio
                this.current_name = it.name
            }
        }
    }

    private fun closeFields() {
        binding.profileEtName.isEnabled = false
        binding.profileEtBio.isEnabled = false
        binding.btnAddImage.isEnabled = false
        binding.profileBtnUpdate.visibility = View.GONE
    }

    private fun openFields() {
        binding.profileEtName.isEnabled = true
        binding.profileEtBio.isEnabled = true
        binding.btnAddImage.isEnabled = true
        binding.profileBtnUpdate.visibility = View.VISIBLE


        binding.btnAddImage.setOnClickListener { cActivityResultLauncher!!.launch("image/*") }

        binding.profileBtnUpdate.setOnClickListener {
            val name: String = binding.profileEtName.text.toString()
            val bio: String = binding.profileEtBio.text.toString()

            if (bio.isEmpty()) {
                binding.profileEtBio.error = "You have to write your bio"
                return@setOnClickListener
            }
            if (name.isEmpty()) {
                binding.profileEtName.error = "You have to write your name"
                return@setOnClickListener
            }
            if (current_bio.equals(bio)) {
                binding.profileEtBio.error = "You have to change bio first"
                return@setOnClickListener
            }
            if (current_name.equals(name)) {
                binding.profileEtName.error = "You have to change name first"
                return@setOnClickListener
            } else {
                addNewData(name, bio)
            }
        }
    }

    private fun addNewData(name: String, bio: String) {
        if (current_name != name) {
            db!!.reference.child("Users").child(mAuth!!.currentUser!!.uid).child("name")
                .setValue(name)
        }
        if (current_bio != bio) {
            db!!.reference.child("Users").child(mAuth!!.currentUser!!.uid).child("bio")
                .setValue(bio)
        }
        if (result_uri != null) {
            storeProfileImage(result_uri!!)
        }
    }

    private fun startCrop(crop_uri: Uri) {
        val time=Date().time.toString()
        val uCrop = UCrop.of(crop_uri, Uri.fromFile(File(cacheDir, "$time.jpg")))
        uCrop.withAspectRatio(5f, 5f)
        uCrop.withMaxResultSize(700, 700)
        uCrop.withOptions(getCropOption()!!)
        uCrop.start(this)
    }

    private fun getCropOption(): UCrop.Options? {
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
                result_uri = result_uri_crop
                Glide.with(this.applicationContext).load(result_uri.toString())
                    .placeholder(R.drawable.ic_person)
                    .into(binding.profileImage)

            }
        }
    }

    private fun storeProfileImage(imageUri: Uri) {
        val reference =
            dbStorage!!.reference.child("Profile_images").child(mAuth!!.currentUser!!.uid)

        reference.putFile(imageUri).addOnSuccessListener {
            reference.downloadUrl.addOnSuccessListener {

                db!!.reference.child("Users").child(mAuth!!.currentUser!!.uid)
                    .child("profileImageUri").setValue(it.toString())
            }
        }
    }
}