package com.wagdybuild.whatsapp.viewModel

import android.app.Application
import android.app.ProgressDialog
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.wagdybuild.whatsapp.repository.AuthRepo
import com.wagdybuild.whatsapp.ui.SignInActivity

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private var currentUser: FirebaseUser? = null
    private var firebaseUserMutableLiveData: MutableLiveData<FirebaseUser>? = null
    private var repo : AuthRepo?=null

    init {
        repo = AuthRepo(application)
        currentUser = repo!!.getCurrentUser()
        firebaseUserMutableLiveData = repo!!.getFirebaseUserMutableLiveData()

    }

    fun getFirebaseUserMutableLiveData(): MutableLiveData<FirebaseUser>?{
        return firebaseUserMutableLiveData
    }

    fun getCurrentUser(): FirebaseUser?{
        return currentUser
    }

    fun createNewUser(email :String, password :String){
        repo!!.createNewUser(email, password)
    }

    fun signIn(email :String, password :String){
        repo!!.signIn(email, password)
    }

    fun signOut(){
        repo!!.signOut()
    }


}