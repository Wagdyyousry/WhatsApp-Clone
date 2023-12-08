package com.wagdybuild.whatsapp.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.wagdybuild.whatsapp.R
import com.wagdybuild.whatsapp.adapters.MainPagerAdapter
import com.wagdybuild.whatsapp.databinding.ActivityMainBinding
import com.wagdybuild.whatsapp.models.Status
import com.wagdybuild.whatsapp.viewModel.FirebaseDatabaseViewModel
import java.util.Date


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var firebaseViewModel: FirebaseDatabaseViewModel
    private lateinit var db: FirebaseDatabase
    private lateinit var mAuth: FirebaseAuth

    @SuppressLint("RemoteViewLayout")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()


        gettingAllStatus()

        gettingMyStatus()

        binding.swipeRefresh.setOnRefreshListener {
            finish()
            overridePendingTransition(0, 0)
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(0, 0)
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun init() {
        //Custom status bar
        window.statusBarColor = ContextCompat.getColor(this, R.color.status_background)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
        setSupportActionBar(binding.mainToolbar)
        supportActionBar?.title = "WhatsAppClone"

        binding.mainViewPager.adapter = MainPagerAdapter(supportFragmentManager, this)
        binding.mainTabLayout.setupWithViewPager(binding.mainViewPager)

        //Firebase
        db = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()

        //users view model
        firebaseViewModel = ViewModelProvider(this)[FirebaseDatabaseViewModel(application)::class.java]

    }

    private fun gettingAllStatus() {
        firebaseViewModel.getOthersStatusLiveData().observe(this) {
            if (it != null && it.size > 0) {
                for (statusList in it) {
                    for (status in statusList) {
                        val deff = calculateDateDiff(status.time)
                        if (deff >= 24) {
                            //Log.d("==============>>>>", status.status_id+"Hours-->"+deff)
                            deleteStatus(status)
                        }
                    }
                }
            }
        }
    }

    private fun gettingMyStatus() {
        firebaseViewModel.getCurrentUserStatusLiveData().observe(this) {
            if (it != null && it.size > 0) {
                for (status in it) {
                    val diff = calculateDateDiff(status.time)
                    if (diff >= 24) {
                        //Log.d("==============>>>>", status.status_id+"Hours-->"+diff)
                        deleteStatus(status)
                    }
                }
            }
        }
    }

    private fun deleteStatus(status: Status) {
         db.reference.child("Status").child(status.user_id).child(status.status_id).removeValue()
    }

    private fun calculateDateDiff(createdDate: Long): Long {
        val diffInMillis = Date().time - Date(createdDate).time

        val days = diffInMillis / 1000 / 60 / 60 / 24
        val hours = diffInMillis / 1000 / 60 / 60
        val minutes = diffInMillis / 1000 / 60
        return hours

        /*val sdf = SimpleDateFormat("dd/M/yyyy HH:MM:ss a")

        val date_old = sdf.format(Date(created_date))
        val date_new = sdf.format(Date(Date().time))

        val old2 = sdf.parse(date_old)
        val new2 = sdf.parse(date_new)*/
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logOut -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this@MainActivity, SignInActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }

            R.id.settings -> {
                startActivity(Intent(this, MyProfileActivity::class.java))
                return true
            }

            R.id.create_new_group -> {
                startActivity(Intent(this, CreateGroupActivity::class.java))
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


}