package com.wagdybuild.whatsapp.ui

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.wagdybuild.whatsapp.R
import com.wagdybuild.whatsapp.databinding.ActivityViewVideoBinding
import com.wagdybuild.whatsapp.models.Message

class ViewVideoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewVideoBinding
    private lateinit var messageList: ArrayList<Message>
    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseDatabase? = null
    private var mc: MediaController? = null
    private var dbStorage: FirebaseStorage? = null
    private var videoUri: Uri? = null
    private var cActivityResultLauncher: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityViewVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { onBackPressed() }

        initialize()

        //intent data
        videoUri = intent.getStringExtra("videoUri")!!.toUri()

        val mediaController = MediaController(this)
        mediaController.setAnchorView(binding.videoView)
        binding.videoView.setMediaController(mediaController)
        binding.videoView.setVideoURI(videoUri)
        binding.videoView.start()

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