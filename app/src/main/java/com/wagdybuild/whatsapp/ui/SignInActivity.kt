package com.wagdybuild.whatsapp.ui

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.wagdybuild.whatsapp.R
import com.wagdybuild.whatsapp.databinding.ActivitySignInBinding
import com.wagdybuild.whatsapp.models.User
import com.wagdybuild.whatsapp.viewModel.AuthViewModel


class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mAuth: FirebaseAuth? = null
    private var loader: ProgressDialog? = null
    private var authViewModel: AuthViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()


        //create new account
        binding.btnCreateNewAccount.setOnClickListener {
            val intent = Intent(this@SignInActivity, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.btnSignIn.setOnClickListener {
            val email: String = binding.signUpEmail.text.toString().trim()
            val password: String = binding.signUpPassword.text.toString().trim()
            if (TextUtils.isEmpty(email)) {
                binding.signUpEmail.error = "You have to write your email"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(password)) {
                binding.signUpPassword.error = "You have to write your password"
                return@setOnClickListener
            } else {
                signIn(email,password)

            }
        }


        /*val gms: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()*/
        val gms: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gms)

        binding.btnGoogle.setOnClickListener {
            val intent: Intent = mGoogleSignInClient!!.signInIntent
            startActivityForResult(intent, 100)
        }
    }

    private fun initialize() {
        //Custom status bar color
        window.statusBarColor= ContextCompat.getColor(this, R.color.status_background)
        window.navigationBarColor= ContextCompat.getColor(this,  R.color.status_background)

        loader = ProgressDialog(this)

        binding.btnCreateNewAccount.paintFlags =
            binding.btnCreateNewAccount.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        //Firebase references
        mAuth = FirebaseAuth.getInstance()

        authViewModel = ViewModelProvider(
            this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(application)
        )[AuthViewModel::class.java]

        //if the current user exist go to main activity directly
        if (mAuth!!.currentUser != null) {
            val intent = Intent(this@SignInActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun signIn(email: String, password: String) {
        loader!!.setMessage("Signing in...")
        loader!!.setCanceledOnTouchOutside(false)
        loader!!.show()

        authViewModel!!.signIn(email, password)
        authViewModel!!.getFirebaseUserMutableLiveData()!!.observe(this) {
            if(it!=null){
                loader!!.dismiss()
                //Toast.makeText(this@SignInActivity, "Signed In Successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check condition
        if (requestCode == 100 ) {
            val signInAccountTask: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                // Initialize sign in account
                val googleSignInAccount: GoogleSignInAccount =
                    signInAccountTask.getResult(ApiException::class.java)
                authSignInWithGoogle(googleSignInAccount)

            } catch (e: ApiException) {
                e.printStackTrace()
            }
        }
    }

    private fun authSignInWithGoogle(googleSignInAccount: GoogleSignInAccount) {
        val authCredential: AuthCredential =
            GoogleAuthProvider.getCredential(googleSignInAccount.idToken, null)

        mAuth!!.signInWithCredential(authCredential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val mUser: FirebaseUser? = mAuth!!.currentUser
                    //val users:Users=Users(mUser.displayName,mUser.email,"sdajkak",mUser!!.uid)
                    //val user= User(mUser!!.displayName,mUser.email,mUser.uid)
                    startActivity(
                        Intent(this@SignInActivity, MainActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }

    }


}