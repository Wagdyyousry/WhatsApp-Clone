package com.wagdybuild.whatsapp.ui

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.wagdybuild.whatsapp.R
import com.wagdybuild.whatsapp.adapters.GroupsChatAdapter
import com.wagdybuild.whatsapp.databinding.ActivityGroupChattingBinding
import com.wagdybuild.whatsapp.models.DBGroupMessage
import com.wagdybuild.whatsapp.models.Message
import com.wagdybuild.whatsapp.models.User
import com.wagdybuild.whatsapp.viewModel.FirebaseDatabaseViewModel
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class GroupChattingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupChattingBinding
    private lateinit var firebaseViewModel: FirebaseDatabaseViewModel
    private lateinit var chattingAdapter: GroupsChatAdapter
    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseDatabase? = null
    private var dbStorage: FirebaseStorage? = null
    private var result_uri: Uri? = null
    private var user: User? = null
    private var group_id: String? = null
    private var select_type: String? = ""
    private var receivers_ids: ArrayList<String>? = null
    private lateinit var btn_image: ImageButton
    private lateinit var btn_video: ImageButton
    private lateinit var btn_file: ImageButton
    private lateinit var btn_music: ImageButton
    private var loader: ProgressDialog? = null
    private var snackLoader: Snackbar? = null

    private var cActivityResultLauncher: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupChattingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()

        gettingCurrentUserData()

        receivers_ids = intent.getStringArrayListExtra("group_members")
        group_id = intent.getStringExtra("group_id")
        val sender_id = mAuth!!.currentUser!!.uid
        val group_name = intent.getStringExtra("group_name")
        val group_createrId = intent.getStringExtra("group_creater_id")
        val group_imageUri = intent.getStringExtra("group_imageUri")

        //Put data in fields
        binding.groupName.text = group_name
        if (group_imageUri != null) {
            Glide.with(this.applicationContext).load(group_imageUri)
                .placeholder(R.drawable.ic_group)
                .into(binding.groupImage)
        }

        binding.btnBack.setOnClickListener { onBackPressed() }


        //Custom dialog create new group
        customDialog()

        //Getting messages
        gettingMessages(group_id!!)

        //sending Message
        binding.btnSend.setOnClickListener { sendingMessages(group_id!!) }

        binding.groupImage.setOnClickListener {
            val intent = Intent(this@GroupChattingActivity, ViewImageActivity::class.java)
            intent.putExtra("imageUri", group_imageUri)
            startActivity(intent)
        }

    }

    private fun initialize() {
        //custom status bar
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)

        loader = ProgressDialog(this)

        //chatting room
        chattingAdapter = GroupsChatAdapter(this, group_id)
        binding.groupRv.layoutManager = LinearLayoutManager(this)
        binding.groupRv.adapter = chattingAdapter

        //Firebase references
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        dbStorage = FirebaseStorage.getInstance()

        receivers_ids = ArrayList()

        //image lips
        cActivityResultLauncher = registerForActivityResult<String, Uri>(
            ActivityResultContracts.GetContent()
        ) {
            if (it != null) {
                when (select_type) {
                    "image" -> {
                        startCrop(it)
                    }

                    "video" -> {
                        sendingVideo(it)
                    }

                    "music" -> {
                        sendingMusic(it)
                    }

                    "file" -> {
                        sendingFile(it)
                    }
                }
            }
        }

        //message list view model
        firebaseViewModel = ViewModelProvider(this)[FirebaseDatabaseViewModel(application)::class.java]
    }

    private fun gettingCurrentUserData() {
        firebaseViewModel.getCurrentUserDataLiveData().observe(this) {
            if (it != null) {
                user = it
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun gettingMessages(group_id: String) {
        firebaseViewModel.gettingGroupMessagesList(group_id)
        firebaseViewModel.getGroupMessagesListLiveData().observe(this) {
            if (it != null && it.isNotEmpty()) {
                chattingAdapter.setMessageList(it as ArrayList<DBGroupMessage>)
                chattingAdapter.notifyDataSetChanged()
                binding.groupRv.scrollToPosition(it.size - 1)
            }
        }

    }

    @SuppressLint("MissingInflatedId")
    private fun customDialog() {
        //Custom dialog create new group
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.select_type_dialog, null)

        btn_image = view.findViewById(R.id.dialog_select_btn_image)
        btn_video = view.findViewById(R.id.dialog_select_btn_video)
        btn_file = view.findViewById(R.id.dialog_select_btn_file)
        btn_music = view.findViewById(R.id.dialog_select_btn_music)

        builder.setView(view)

        val dialog = builder.create()
        binding.btnFile.setOnClickListener { dialog.show() }

        btn_video.setOnClickListener {
            select_type = "video"
            cActivityResultLauncher!!.launch("video/*")
            dialog.dismiss()
        }

        btn_image.setOnClickListener {
            select_type = "image"
            cActivityResultLauncher!!.launch("image/*")
            dialog.dismiss()
        }

        btn_file.setOnClickListener {
            select_type = "file"
            cActivityResultLauncher!!.launch("file/*")
            dialog.dismiss()
        }

        btn_music.setOnClickListener {
            select_type = "music"
            cActivityResultLauncher!!.launch("music/*")
            dialog.dismiss()
        }
    }

    private fun sendingMessages(group_id: String) {
        val message = binding.etMessage.text.toString()
        if (message.isNotEmpty()) {
            val time = Date().time
            val message_model = Message()
            message_model.message = message
            message_model.sender_id = mAuth!!.currentUser!!.uid
            message_model.time = time
            message_model.receiver_id = group_id
            message_model.message_id = group_id
            message_model.sender_imageUri = user!!.profileImageUri
            message_model.sender_name = user!!.name
            message_model.message_type = "message"


            binding.etMessage.setText("")

            db!!.reference.child("Groups_Chat")
                .child(group_id)
                .push()
                .setValue(message_model)

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

                sendingImage(result_uri!!)
            }
        }
    }

    private fun sendingImage(imageUri: Uri) {
        loader!!.setTitle("Uploading image ...")
        loader!!.setMessage("Please waite few seconds...")
        loader!!.setCanceledOnTouchOutside(false)
        loader!!.show()

        val time = Date().time.toString()
        val reference = dbStorage!!.reference.child("Groups_chat").child("images")
            .child(group_id!!).child(mAuth!!.currentUser!!.uid).child(time)

        reference.putFile(imageUri).addOnProgressListener {
            val percent = (100.0 * it.bytesTransferred) / it.totalByteCount
            val progress = percent.toInt()
            loader!!.setMessage("$progress % Uploading ...")
        }.addOnSuccessListener {
            reference.downloadUrl.addOnSuccessListener {

                val time: Long = Date().time
                val message_model = Message()
                message_model.time = time
                message_model.imageUri = it.toString()
                message_model.sender_id = mAuth!!.currentUser!!.uid
                message_model.message_id = group_id!!
                message_model.message_type = "image"

                db!!.reference.child("Groups_Chat")
                    .child(group_id!!)
                    .push()
                    .setValue(message_model).addOnSuccessListener {
                        Toast.makeText(this@GroupChattingActivity, "Done", Toast.LENGTH_SHORT)
                            .show()
                        loader!!.dismiss()
                    }

            }
        }


    }

    private fun sendingVideo(it: Uri) {
        loader!!.setTitle("Uploading video ...")
        loader!!.setMessage("Please waite few seconds...")
        loader!!.setCanceledOnTouchOutside(false)
        loader!!.show()

        val time = Date().time.toString()
        val reference =
            dbStorage!!.reference.child("Groups_chat").child("videos").child(group_id!!)
                .child(mAuth!!.currentUser!!.uid).child(time)

        reference.putFile(it).addOnProgressListener {
            val percent = (100.0 * it.bytesTransferred) / it.totalByteCount
            val progress = percent.toInt()
            loader!!.setMessage("$progress % Uploading ...")
        }.addOnSuccessListener {

            reference.downloadUrl.addOnSuccessListener {

                val time: Long = Date().time
                val message_model = Message()
                message_model.time = time
                message_model.imageUri = it.toString()
                message_model.sender_id = mAuth!!.currentUser!!.uid
                message_model.message_id = group_id!!
                message_model.message_type = "video"

                db!!.reference.child("Groups_Chat")
                    .child(group_id!!)
                    .push()
                    .setValue(message_model).addOnSuccessListener {
                        Toast.makeText(this@GroupChattingActivity, "Done", Toast.LENGTH_SHORT)
                            .show()
                        loader!!.dismiss()
                    }

            }
        }
    }

    private fun sendingFile(it: Uri) {
        loader!!.setTitle("Uploading Audio ...")
        loader!!.setMessage("Please waite few seconds...")
        loader!!.setCanceledOnTouchOutside(false)
        loader!!.show()

        val time = Date().time.toString()
        val reference =
            dbStorage!!.reference.child("Groups_chat").child("files").child(group_id!!)
                .child(mAuth!!.currentUser!!.uid).child(time)

        reference.putFile(it).addOnProgressListener {
            val percent = (100.0 * it.bytesTransferred) / it.totalByteCount
            val progress = percent.toInt()
            loader!!.setMessage("$progress % Uploading ...")
        }.addOnSuccessListener {

            reference.downloadUrl.addOnSuccessListener {

                val time: Long = Date().time
                val message_model = Message()
                message_model.time = time
                message_model.imageUri = it.toString()
                message_model.sender_id = mAuth!!.currentUser!!.uid
                message_model.message_id = group_id!!
                message_model.message_type = "file"

                db!!.reference.child("Groups_Chat")
                    .child(group_id!!)
                    .push()
                    .setValue(message_model).addOnSuccessListener {
                        Toast.makeText(this@GroupChattingActivity, "Done", Toast.LENGTH_SHORT)
                            .show()
                        loader!!.dismiss()
                    }

            }
        }
    }

    private fun sendingMusic(it: Uri) {
        loader!!.setTitle("Uploading image ...")
        loader!!.setMessage("Please waite few seconds...")
        loader!!.setCanceledOnTouchOutside(false)
        loader!!.show()

        val time = Date().time.toString()
        val reference =
            dbStorage!!.reference.child("Groups_chat").child("music").child(group_id!!)
                .child(mAuth!!.currentUser!!.uid).child(time)

        reference.putFile(it).addOnProgressListener {
            val percent = (100.0 * it.bytesTransferred) / it.totalByteCount
            val progress = percent.toInt()
            loader!!.setMessage("$progress % Uploading ...")
        }.addOnSuccessListener {

            reference.downloadUrl.addOnSuccessListener {

                val time: Long = Date().time
                val message_model = Message()
                message_model.time = time
                message_model.imageUri = it.toString()
                message_model.sender_id = mAuth!!.currentUser!!.uid
                message_model.message_id = group_id!!
                message_model.message_type = "music"

                db!!.reference.child("Groups_Chat")
                    .child(group_id!!)
                    .push()
                    .setValue(message_model).addOnSuccessListener {
                        Toast.makeText(this@GroupChattingActivity, "Done", Toast.LENGTH_SHORT)
                            .show()
                        loader!!.dismiss()
                    }

            }
        }
    }
}