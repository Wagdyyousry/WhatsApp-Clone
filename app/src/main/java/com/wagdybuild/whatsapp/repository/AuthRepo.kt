package com.wagdybuild.whatsapp.repository

import android.app.Application
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.wagdybuild.whatsapp.databinding.ActivityAddStatusImageBinding
import com.wagdybuild.whatsapp.models.Message
import com.wagdybuild.whatsapp.models.User
import com.wagdybuild.whatsapp.ui.MainActivity
import com.wagdybuild.whatsapp.ui.SignInActivity

class AuthRepo(application: Application) {
    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null
    private var firebaseUserMutableLiveData: MutableLiveData<FirebaseUser>? = null
    private var application: Application? = null
    //private var loader: ProgressDialog? = null

    init {
        this.application = application
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth!!.currentUser
        firebaseUserMutableLiveData = MutableLiveData()
        //loader = ProgressDialog(application)
    }

    fun getFirebaseUserMutableLiveData(): MutableLiveData<FirebaseUser>? {
        return firebaseUserMutableLiveData
    }

    fun getCurrentUser(): FirebaseUser? {
        return mAuth!!.currentUser
    }

    fun createNewUser(email: String, password: String) {

        mAuth!!.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                firebaseUserMutableLiveData!!.postValue(it.result.user)
            } else {
                Toast.makeText(application, "Error ,${it.exception!!.message}", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    fun signIn(email: String, password: String) {
        mAuth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(application, "Signed In Successfully", Toast.LENGTH_SHORT).show()
                    firebaseUserMutableLiveData!!.postValue(it.result.user)

                    //application!!.startActivity(Intent(application, MainActivity::class.java))
                } else {
                    Toast.makeText(
                        application,
                        "Error ,${it.exception!!.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }
    }

    fun signOut() {
        application!!.startActivity(Intent(application, SignInActivity::class.java))

        mAuth!!.signOut()

        Toast.makeText(application, "Signed Out Successfully", Toast.LENGTH_LONG).show()

    }
}