package com.wagdybuild.whatsapp.ui

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.wagdybuild.whatsapp.models.Message
import com.wagdybuild.whatsapp.R
import com.wagdybuild.whatsapp.databinding.ActivityAddStatusImageBinding
import com.wagdybuild.whatsapp.models.Status
import com.wagdybuild.whatsapp.models.User
import com.wagdybuild.whatsapp.viewModel.FirebaseDatabaseViewModel
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class AddStatusImageActivity : AppCompatActivity() {
    private lateinit var firebaseViewModel: FirebaseDatabaseViewModel
    private lateinit var binding: ActivityAddStatusImageBinding
    private lateinit var messageList: ArrayList<Message>
    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseDatabase? = null
    private var dbStorage: FirebaseStorage? = null
    private var result_uri_base: Uri? = null
    private var sender_room: String? = null
    private var receiver_room: String? = null
    private var receiver_id: String = ""
    private var user:User?= null
    private var loader: ProgressDialog? = null
    private var cActivityResultLauncher: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStatusImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //initialization for all variables
        initialize()

        //getting current user data
        gettingCurrentUserData()

        //intent data
        result_uri_base = intent.getStringExtra("image")!!.toUri()
        Glide.with(this@AddStatusImageActivity)
            .load(result_uri_base.toString())
            .placeholder(R.drawable.ic_person)
            .into(binding.imageView)

        binding.btnBack.setOnClickListener { onBackPressed() }

        binding.btnCamera.setOnClickListener {
            cActivityResultLauncher!!.launch("image/*")
        }

        binding.btnCrop.setOnClickListener {
            startCrop(result_uri_base!!)
        }

        binding.fabSubmitImage.setOnClickListener {
            if (result_uri_base != null) {
                storeStatusImage(result_uri_base!!)
            }
        }

    }

    private fun initialize() {
        //custom status bar
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)

        loader = ProgressDialog(this)

        //Firebase references
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        dbStorage = FirebaseStorage.getInstance()

        //image lips
        cActivityResultLauncher = registerForActivityResult<String, Uri>(
            ActivityResultContracts.GetContent()
        ) {
            binding.imageView.setImageURI(it)
        }
        //user data view model
        firebaseViewModel = ViewModelProvider(this)[FirebaseDatabaseViewModel(application)::class.java]
    }

    private fun gettingCurrentUserData() {
        firebaseViewModel.getCurrentUserDataLiveData().observe(this) {
            if (it != null) {
                user = it
            }
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
                result_uri_base = result_uri_crop
                binding.imageView.setImageURI(result_uri_base)
            }
        }
    }

    private fun storeStatusImage(imageUri: Uri) {
        loader!!.setTitle("Uploading status ...")
        loader!!.setMessage("Please waite few seconds...")
        loader!!.setCanceledOnTouchOutside(false)
        loader!!.show()

        val reference =
            dbStorage!!.reference.child("status_images").child(mAuth!!.currentUser!!.uid).child(Date().time.toString())

        reference.putFile(imageUri).addOnProgressListener {
            val  percent = ( 100.0 * it.bytesTransferred ) / it.totalByteCount
            val  progress = percent.toInt()
            loader!!.setMessage(   "$progress % Uploading ...")
        }
            .addOnSuccessListener {
            reference.downloadUrl.addOnSuccessListener {
                val time: Long = Date().time
                val status = Status(
                    mAuth!!.currentUser!!.uid,
                    mAuth!!.currentUser!!.uid,
                    time,
                    "image",
                    binding.statusCaption.text.toString(),
                    it.toString(),
                    "",
                    user!!.name
                )
                db!!.reference.child("Status")
                    .child(mAuth!!.currentUser!!.uid)
                    .push()
                    .setValue(status).addOnSuccessListener {
                        Toast.makeText(this, "Status successfully uploaded", Toast.LENGTH_LONG)
                            .show()
                        startActivity(Intent(this@AddStatusImageActivity, MainActivity::class.java))
                        loader!!.dismiss()
                    }

            }
        }


    }
}