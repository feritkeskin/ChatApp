package com.feritkeskin.chatapp.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.feritkeskin.chatapp.R
import com.feritkeskin.chatapp.RetrofitInstance
import com.feritkeskin.chatapp.adapter.ChatAdapter
import com.feritkeskin.chatapp.databinding.ActivityChatBinding
import com.feritkeskin.chatapp.model.Chat
import com.feritkeskin.chatapp.model.NotificationData
import com.feritkeskin.chatapp.model.PushNotification
import com.feritkeskin.chatapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    var firebaseUser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    var chatList = ArrayList<Chat>()
    var topic = ""

    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        val view = binding.root

        firebaseUser = FirebaseAuth.getInstance().currentUser
        binding.chatRecyclerView.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        databaseReference =
            FirebaseDatabase.getInstance().getReference("users").child(firebaseUser?.uid.orEmpty())
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                println("kullanici fotosu: ${user?.profileImage}")
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }
        })

        setContentView(view)

        val intent = intent
        val userId = intent.getStringExtra("userId")
        val userName = intent.getStringExtra("userName")

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        reference = FirebaseDatabase.getInstance().getReference("users").child(userId!!)

        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val user = snapshot.getValue(User::class.java)
                binding.tvUserName.text = user?.userName
                if (user?.profileImage == "") {
                    binding.imgProfile.setImageResource(R.drawable.profile_image)
                } else {
                    Glide.with(this@ChatActivity).load(user?.profileImage).into(binding.imgProfile)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        binding.btnSendMessage.setOnClickListener {

            val message: String = binding.etMessage.text.toString()

            if (message.isEmpty()) {
                Toast.makeText(applicationContext, "Mesaj boş bırakılamaz..!", Toast.LENGTH_SHORT)
                    .show()
                binding.etMessage.setText("")
            } else {
                sendMessage(firebaseUser!!.uid, userId, message)
                binding.etMessage.setText("")
                topic = "/topics/$userId"
                Log.d(this.javaClass.simpleName, "onCreate: topic: $topic, userName: $userName")
                PushNotification(
                    NotificationData(userName!!, message),
                    topic
                ).also {
                    //sendNotification(it)
                }
            }
        }

        readMessage(firebaseUser!!.uid, userId)

    }

    private fun sendMessage(senderId: String, receiverId: String, message: String) {

        val reference: DatabaseReference? = FirebaseDatabase.getInstance().getReference()

        val hashMap: HashMap<String, String> = HashMap()
        hashMap["senderId"] = senderId
        hashMap["receiverId"] = receiverId
        hashMap["message"] = message

        reference?.child("Chat")?.push()?.setValue(hashMap)

    }

    private fun readMessage(senderId: String, receiverId: String) {

        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Chat")

        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()

                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val chat = dataSnapShot.getValue(Chat::class.java)

                    if (chat?.senderId.equals(senderId) && chat?.receiverId.equals(receiverId) ||
                        chat?.senderId.equals(receiverId) && chat?.receiverId.equals(senderId)
                    ) {
                        if (chat != null) {
                            chatList.add(chat)
                        }
                    }
                }

                val chatAdapter = ChatAdapter(this@ChatActivity, chatList)

                binding.chatRecyclerView.adapter = chatAdapter
                binding.chatRecyclerView.scrollToPosition(chatList.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}