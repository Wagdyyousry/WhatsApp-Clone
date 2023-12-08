package com.wagdybuild.whatsapp.ui

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.wagdybuild.whatsapp.R
import com.wagdybuild.whatsapp.databinding.ActivityAddStatusImageBinding
import com.wagdybuild.whatsapp.databinding.ActivityViewImageBinding
import com.wagdybuild.whatsapp.models.Message

class ViewImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewImageBinding
    private lateinit var messageList: ArrayList<Message>
    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseDatabase? = null
    private var dbStorage: FirebaseStorage? = null
    private var imageUri: Uri? = null
    private var cActivityResultLauncher: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityViewImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()

        //intent data
        imageUri = intent.getStringExtra("imageUri")!!.toUri()
        Glide.with(this@ViewImageActivity)
            .load(imageUri.toString())
            .placeholder(R.drawable.ic_person)
            .into(binding.imageView)

        binding.btnBack.setOnClickListener { onBackPressed() }
    }
    private fun initialize() {
        //custom status bar
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)

        //Firebase references
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        dbStorage = FirebaseStorage.getInstance()


    }
}