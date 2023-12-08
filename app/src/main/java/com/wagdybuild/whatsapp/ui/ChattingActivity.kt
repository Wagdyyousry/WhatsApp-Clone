package com.wagdybuild.whatsapp.ui

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.wagdybuild.whatsapp.R
import com.wagdybuild.whatsapp.adapters.FriendsChatAdapter
import com.wagdybuild.whatsapp.databinding.ActivityChattingBinding
import com.wagdybuild.whatsapp.models.DBFriendMessage
import com.wagdybuild.whatsapp.models.Message
import com.wagdybuild.whatsapp.viewModel.FirebaseDatabaseViewModel
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.*


class ChattingActivity : AppCompatActivity() {
    private lateinit var firebaseListViewModel: FirebaseDatabaseViewModel
    private var mAuth: FirebaseAuth? = null
    private var mDatabase: FirebaseDatabase? = null
    private var dbStorage: FirebaseStorage? = null
    private lateinit var binding: ActivityChattingBinding
    private lateinit var chattingAdapter: FriendsChatAdapter
    private lateinit var btn_image: ImageButton
    private lateinit var btn_video: ImageButton
    private lateinit var btn_file: ImageButton
    private lateinit var btn_music: ImageButton
    private var select_type: String? = ""
    private var result_uri: Uri? = null
    private var sender_room: String? = null
    private var receiver_room: String? = null
    private var receiver_id: String = ""
    private var loader: ProgressDialog? = null

    private var cActivityResultLauncher: ActivityResultLauncher<String>? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChattingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()

        val sender_id = mAuth!!.currentUser!!.uid
        receiver_id = intent.getStringExtra("receiver_id").toString()
        val receiver_name = intent.getStringExtra("receiver_name")
        val receiver_email = intent.getStringExtra("receiver_email")
        val receiver_password = intent.getStringExtra("receiver_password")
        val receiver_profile_image = intent.getStringExtra("receiver_profile_image")
        val receiver_last_message = intent.getStringExtra("receiver_last_message")


        //put data in fields
        binding.chattingTvName.text = receiver_name
        if (receiver_profile_image != null) {
            Glide.with(this.applicationContext).load(receiver_profile_image)
                .placeholder(R.drawable.ic_person)
                .into(binding.userViewImage)
        }

        sender_room = sender_id + receiver_id
        receiver_room = receiver_id + sender_id

        //chatting room
        chattingAdapter = FriendsChatAdapter(this, receiver_id)
        binding.chattingRv.layoutManager = LinearLayoutManager(this)
        binding.chattingRv.adapter = chattingAdapter

        //Getting messages
        gettingMessages(receiver_id)

        //sending Message
        binding.chattingBtnSend.setOnClickListener { sendingMessages(receiver_id) }

        binding.chattingBtnBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        customDialog()

        binding.userViewImage.setOnClickListener {
            val intent = Intent(this@ChattingActivity, ViewImageActivity::class.java)
            intent.putExtra("imageUri", receiver_profile_image)
            startActivity(intent)
        }
    }

    private fun initialize() {
        //custom status bar
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)

        loader = ProgressDialog(this)
        //Firebase references
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance()
        dbStorage = FirebaseStorage.getInstance()

        //users view model
        firebaseListViewModel =
            ViewModelProvider(this)[FirebaseDatabaseViewModel(application)::class.java]

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
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun gettingMessages(receiverId: String) {
        firebaseListViewModel.gettingChatMessagesList(receiverId)
        firebaseListViewModel.getChatMessagesListLiveData().observe(this) {
            val list = it as ArrayList<DBFriendMessage>
            if (list.isNotEmpty()) {
                chattingAdapter.setMessageList(list)
                chattingAdapter.notifyDataSetChanged()
                binding.chattingRv.scrollToPosition(list.size - 1)
                //binding.chattingRv.smoothScrollToPosition(binding.chattingRv.adapter!!.itemCount - 1);
            }

        }


        /*mDatabase!!.reference.child("Chats")
            .child(mAuth!!.currentUser!!.uid)
            .child(receiverId).addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val message_model = dataSnapshot.getValue(Message::class.java)
                        message_model!!.message_id = dataSnapshot.key.toString()
                        messageList.add(message_model)
                    }
                    chattingAdapter.notifyDataSetChanged()
                    binding.chattingRv.scrollToPosition(messageList.size - 1)

                }

                override fun onCancelled(error: DatabaseError) {
                }
            })*/

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
        binding.chattingBtnFile.setOnClickListener { dialog.show() }

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

    private fun sendingMessages(receiverId: String) {
        val message = binding.chattingEtMessage.text.toString()
        if (message.isNotEmpty()) {
            val time = Date().time
            val message_model = Message()
            message_model.message = message
            message_model.message_id = sender_room!!
            message_model.sender_id = mAuth!!.currentUser!!.uid
            message_model.receiver_id = receiver_id
            message_model.time = time
            message_model.message_type = "message"

            binding.chattingEtMessage.setText("")

            mDatabase!!.reference.child("Chats")
                .child(mAuth!!.currentUser!!.uid)
                .child(receiverId)
                .push()
                .setValue(message_model).addOnSuccessListener {
                    mDatabase!!.reference.child("Chats")
                        .child(receiverId)
                        .child(mAuth!!.currentUser!!.uid)
                        .push()
                        .setValue(message_model)
                }

        }

    }

    @SuppressLint("SetTextI18n")
    private fun sendingImage(imageUri: Uri) {
        binding.seekbarLayout.visibility = View.VISIBLE
        binding.tvSeekBar.text = "Uploading image ..."
        binding.seekbar.isEnabled = false

        val time = Date().time.toString()

        val reference =
            dbStorage!!.reference.child("Users_chat").child("images")
                .child(mAuth!!.currentUser!!.uid).child(receiver_id).child(time)

        reference.putFile(imageUri).addOnProgressListener {
            val percent = (100.0 * it.bytesTransferred) / it.totalByteCount
            val progress = percent.toInt()
            //loader!!.setMessage("$progress % Uploading ...")
            binding.progressPercentage.text = "$progress %"
            binding.seekbar.progress = progress


        }.addOnSuccessListener {
            reference.downloadUrl.addOnSuccessListener {

                val time: Long = Date().time
                val message_model = Message()
                message_model.message_id = sender_room!!
                message_model.sender_id = mAuth!!.currentUser!!.uid
                message_model.receiver_id = receiver_id
                message_model.time = time
                message_model.message_type = "image"
                message_model.imageUri = it.toString()

                mDatabase!!.reference.child("Chats")
                    .child(mAuth!!.currentUser!!.uid)
                    .child(receiver_id)
                    .push()
                    .setValue(message_model).addOnSuccessListener {
                        mDatabase!!.reference.child("Chats")
                            .child(receiver_id)
                            .child(mAuth!!.currentUser!!.uid)
                            .push()
                            .setValue(message_model).addOnSuccessListener {
                                Toast.makeText(this@ChattingActivity, "Done", Toast.LENGTH_SHORT)
                                    .show()
                                //loader!!.dismiss()
                                binding.seekbarLayout.visibility = View.GONE
                            }
                    }

            }
        }


    }

    @SuppressLint("SetTextI18n")
    private fun sendingVideo(video_uri: Uri) {
        binding.seekbarLayout.visibility = View.VISIBLE
        binding.tvSeekBar.text = "Uploading video ..."
        binding.seekbar.isEnabled = false

        val time = Date().time.toString()
        val reference =
            dbStorage!!.reference.child("Users_chat").child("videos")
                .child(mAuth!!.currentUser!!.uid).child(receiver_id).child(time)

        reference.putFile(video_uri).addOnProgressListener {
            val percent = (100.0 * it.bytesTransferred) / it.totalByteCount
            val progress = percent.toInt()
            //loader!!.setMessage("$progress % Uploading ...")
            binding.progressPercentage.text = "$progress %"
            binding.seekbar.progress = progress
        }
            .addOnSuccessListener {
                reference.downloadUrl.addOnSuccessListener {

                    val time: Long = Date().time
                    val message_model = Message()
                    message_model.message_id = sender_room!!
                    message_model.sender_id = mAuth!!.currentUser!!.uid
                    message_model.receiver_id = receiver_id
                    message_model.time = time
                    message_model.message_type = "video"
                    message_model.imageUri = it.toString()

                    mDatabase!!.reference.child("Chats")
                        .child(mAuth!!.currentUser!!.uid)
                        .child(receiver_id)
                        .push()
                        .setValue(message_model).addOnSuccessListener {
                            mDatabase!!.reference.child("Chats")
                                .child(receiver_id)
                                .child(mAuth!!.currentUser!!.uid)
                                .push()
                                .setValue(message_model).addOnSuccessListener {
                                    Toast.makeText(
                                        this@ChattingActivity,
                                        "Done",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    //loader!!.dismiss()
                                    binding.seekbarLayout.visibility = View.GONE
                                }
                        }

                }
            }


    }

    private fun sendingFile(file_uri: Uri) {
        binding.seekbarLayout.visibility = View.VISIBLE
        binding.tvSeekBar.text = "Uploading File ..."
        binding.seekbar.isEnabled = false

        val time = Date().time.toString()
        val reference =
            dbStorage!!.reference.child("Users_chat").child("files")
                .child(mAuth!!.currentUser!!.uid).child(receiver_id).child(time)

        reference.putFile(file_uri).addOnProgressListener {
            val percent = (100.0 * it.bytesTransferred) / it.totalByteCount
            val progress = percent.toInt()
            // loader!!.setMessage("$progress % Uploading ...")
            binding.progressPercentage.text = "$progress %"
            binding.seekbar.progress = progress
        }
            .addOnSuccessListener {
                reference.downloadUrl.addOnSuccessListener {

                    val time: Long = Date().time
                    val message_model = Message()
                    message_model.message_id = sender_room!!
                    message_model.sender_id = mAuth!!.currentUser!!.uid
                    message_model.receiver_id = receiver_id
                    message_model.time = time
                    message_model.message_type = "file"
                    message_model.imageUri = it.toString()

                    mDatabase!!.reference.child("Chats")
                        .child(mAuth!!.currentUser!!.uid)
                        .child(receiver_id)
                        .push()
                        .setValue(message_model).addOnSuccessListener {
                            mDatabase!!.reference.child("Chats")
                                .child(receiver_id)
                                .child(mAuth!!.currentUser!!.uid)
                                .push()
                                .setValue(message_model).addOnSuccessListener {
                                    Toast.makeText(
                                        this@ChattingActivity,
                                        "Done",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    //loader!!.dismiss()
                                    binding.seekbarLayout.visibility = View.GONE
                                }
                        }

                }
            }
    }

    private fun sendingMusic(music_uri: Uri) {
        binding.seekbarLayout.visibility = View.VISIBLE
        binding.seekbar.isEnabled = false

        binding.tvSeekBar.text = "Uploading Music ..."

        val time = Date().time.toString()
        val reference =
            dbStorage!!.reference.child("Users_chat").child("music")
                .child(mAuth!!.currentUser!!.uid).child(receiver_id).child(time)

        reference.putFile(music_uri).addOnProgressListener {
            val percent = (100.0 * it.bytesTransferred) / it.totalByteCount
            val progress = percent.toInt()
            // loader!!.setMessage("$progress % Uploading ...")
            binding.progressPercentage.text = "$progress %"
            binding.seekbar.progress = progress
        }
            .addOnSuccessListener {
                reference.downloadUrl.addOnSuccessListener {

                    val time: Long = Date().time
                    val message_model = Message()
                    message_model.message_id = sender_room!!
                    message_model.sender_id = mAuth!!.currentUser!!.uid
                    message_model.receiver_id = receiver_id
                    message_model.time = time
                    message_model.message_type = "music"
                    message_model.imageUri = it.toString()

                    mDatabase!!.reference.child("Chats")
                        .child(mAuth!!.currentUser!!.uid)
                        .child(receiver_id)
                        .push()
                        .setValue(message_model).addOnSuccessListener {
                            mDatabase!!.reference.child("Chats")
                                .child(receiver_id)
                                .child(mAuth!!.currentUser!!.uid)
                                .push()
                                .setValue(message_model).addOnSuccessListener {
                                    Toast.makeText(
                                        this@ChattingActivity,
                                        "Done",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    //loader!!.dismiss()
                                    binding.seekbarLayout.visibility = View.GONE
                                }
                        }

                }
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


}
