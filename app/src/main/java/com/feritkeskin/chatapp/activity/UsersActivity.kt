package com.feritkeskin.chatapp.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.feritkeskin.chatapp.R
import com.feritkeskin.chatapp.adapter.UserAdapter
import com.feritkeskin.chatapp.databinding.ActivityUsersBinding
import com.feritkeskin.chatapp.firebase.FirebaseService
import com.feritkeskin.chatapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging

class UsersActivity : AppCompatActivity() {

    lateinit var binding: ActivityUsersBinding
    var userList = ArrayList<User>()
    private var firebaseUser: FirebaseUser? = null
    private lateinit var mDatabaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        FirebaseService.sharedPref = getSharedPreferences("sharePref", Context.MODE_PRIVATE)
        FirebaseInstallations.getInstance().getToken(true).addOnSuccessListener {
            FirebaseService.token = it.token
        }

        binding.userRecyclerView.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        binding.imgProfile.setOnClickListener {
            val intent = Intent(this@UsersActivity, ProfileActivity::class.java)
            startActivity(intent)
        }
        getUsersList()
    }

    fun getUsersList() {
        val firebase: FirebaseUser? = FirebaseAuth.getInstance().currentUser

        val userid = firebase?.uid
        println("ferit auth" + userid)
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/$userid")

        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("users")

        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                val currentUser = snapshot.getValue(User::class.java)
                if (currentUser?.profileImage == "") {
                    binding.imgProfile.setImageResource(R.drawable.profile_image)
                } else {
                    Glide.with(this@UsersActivity).load(currentUser?.profileImage)
                        .into(binding.imgProfile)
                }

                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val user = dataSnapShot.getValue(User::class.java)

                    if (!user!!.userId.equals(userid)) {
                        userList.add(user)
                    }
                }

                val userAdapter = UserAdapter(this@UsersActivity, userList)

                binding.userRecyclerView.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }
        })

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        mDatabaseReference =
            FirebaseDatabase.getInstance().getReference("users").child(firebaseUser?.uid.orEmpty())
        mDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user?.profileImage == "") {
                    binding.imgProfile.setImageResource(R.drawable.profile_image)
                } else {
                    Glide.with(this@UsersActivity).load(user?.profileImage).into(binding.imgProfile)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Hata çıktı ${error.message}")
            }
        })
    }
}