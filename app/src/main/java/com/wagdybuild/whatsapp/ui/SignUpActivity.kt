package com.wagdybuild.whatsapp.ui

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.wagdybuild.whatsapp.models.User
import com.wagdybuild.whatsapp.R
import com.wagdybuild.whatsapp.databinding.ActivitySignUpBinding
import com.wagdybuild.whatsapp.viewModel.AuthViewModel
import java.util.*

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private var mAuth: FirebaseAuth? = null
    private var mDatabase: FirebaseDatabase? = null
    private var loader: ProgressDialog? = null
    private var authViewModel: AuthViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()


        //Signing up new user
        binding.btnSignUp.setOnClickListener {
            val email: String = Objects.requireNonNull(binding.signUpEmail.text).toString().trim()
            val password: String =
                Objects.requireNonNull(binding.signUpPassword.text).toString().trim()
            val name: String = Objects.requireNonNull(binding.signUpName.text).toString().trim()

            if (TextUtils.isEmpty(email)) {
                binding.signUpEmail.error = "You have to write email"
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.signUpEmail.error = "Wrong email Format"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(password)) {
                binding.signUpPassword.error = "You must write a password"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(name)) {
                binding.signUpName.error = "Your must enter your Name"
                return@setOnClickListener
            } else {
                signUp(email, password, name)
            }
        }
    }

    private fun initialize() {
        //Custom status bar
        window.statusBarColor = ContextCompat.getColor(this, R.color.status_background)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.status_background)

        //Firebase references
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance()

        loader = ProgressDialog(this)

        authViewModel = ViewModelProvider(
            this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(application)
        )[AuthViewModel::class.java]

    }

    private fun signUp(email: String, password: String, name: String) {
        loader?.setMessage("Registering ...")
        loader?.setCanceledOnTouchOutside(false)
        loader?.show()

        authViewModel!!.createNewUser(email, password)
        authViewModel!!.getFirebaseUserMutableLiveData()!!.observe(this) { it1 ->
            if(it1!=null){
              val newUser = User(name,email, password, it1.uid)

              mDatabase!!.reference.child("Users").child(it1.uid).setValue(newUser)
                  .addOnCompleteListener {
                      if (it.isSuccessful) {
                          startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
                          Toast.makeText(this@SignUpActivity, "Registered Successfully", Toast.LENGTH_LONG)
                              .show()

                          finish()
                          loader!!.dismiss()
                      } else {
                          Toast.makeText(this@SignUpActivity, "Error ,${it.exception!!.message}", Toast.LENGTH_LONG).show()
                      }
                      finish()
                  }
          }
        }


        /*loader?.setMessage("Registering ...")
        loader?.setCanceledOnTouchOutside(false)
        loader?.show()
        mAuth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val currentUserId: String = task.result.user!!.uid

                val newUser = User(name,email, password, currentUserId)

                mDatabase!!.reference.child("Users").child(currentUserId).setValue(newUser)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                            startActivity(intent)
                            Toast.makeText(
                                this@SignUpActivity,
                                "Registered Successfully",
                                Toast.LENGTH_LONG
                            ).show()

                            finish()
                            loader!!.dismiss()
                        } else {
                            Toast.makeText(
                                this@SignUpActivity,
                                "Error ," + it.exception!!.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        finish()
                    }

            }
            else {
                Toast.makeText(
                    this@SignUpActivity,
                    "Error ," + task.exception!!.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }*/
    }

}