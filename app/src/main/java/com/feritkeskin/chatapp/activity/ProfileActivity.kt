package com.feritkeskin.chatapp.activity

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.feritkeskin.chatapp.R
import com.feritkeskin.chatapp.databinding.ActivityProfileBinding
import com.feritkeskin.chatapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var firebaseUser: FirebaseUser? = null
    private lateinit var databaseReference: DatabaseReference
    private var filePath: Uri? = null
    private val PICK_IMAGE_REQUEST: Int = 2020
    private lateinit var stroage: FirebaseStorage
    private lateinit var stroageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        databaseReference =
            FirebaseDatabase.getInstance().getReference("users").child(firebaseUser?.uid.orEmpty())

        stroage = FirebaseStorage.getInstance()
        stroageRef = stroage.reference

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val user = snapshot.getValue(User::class.java)

                binding.etUserName.setText(user?.userName.toString())

                if (user?.profileImage == "") {
                    binding.userImage.setImageResource(R.drawable.profile_image)
                } else {
                    Glide.with(this@ProfileActivity).load(user?.profileImage)
                        .into(binding.userImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }
        })

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.userImage.setOnClickListener {
            chooseImage()
        }

        binding.saveButton.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            uploadImage()
        }

        binding.etUserName.addTextChangedListener {
            binding.saveButton.visibility = View.VISIBLE
        }

        binding.tbMenu.inflateMenu(R.menu.main_menu)//Menu ile bağlan.
        binding.tbMenu.setOnMenuItemClickListener {
            if (it.itemId == R.id.exit) {
                Toast.makeText(this, "Çıkış Yapıldı", Toast.LENGTH_SHORT).show()
                Firebase.auth.signOut() //Firebaseden çıkış.
                val intent = Intent(this, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else if (it.itemId == R.id.delete_account) {
                val intent = Intent(this@ProfileActivity, DeleteAccountActivity::class.java)
                startActivity(intent)
            }
            false
        }
    }

    private fun chooseImage() {
        val intent: Intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, " Select Image"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode != null && data != null) {
            filePath = data!!.data

            try {
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                binding.userImage.setImageBitmap(bitmap)
                binding.saveButton.visibility = View.VISIBLE
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage() {
        if (filePath != null) {

            val ref: StorageReference = stroageRef.child("image/" + UUID.randomUUID().toString())

            ref.putFile(filePath!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnCompleteListener {
                        val hashMap: HashMap<String, String> = HashMap()
                        hashMap.put("userName", binding.etUserName.text.toString())
                        hashMap.put("profileImage", it.result.toString())
                        databaseReference.updateChildren(hashMap as Map<String, String>)
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(applicationContext, "Kayıt Başarılı", Toast.LENGTH_SHORT)
                            .show()
                        binding.saveButton.visibility = View.GONE
                    }
                }
                .addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(applicationContext, "Failed" + it.message, Toast.LENGTH_SHORT)
                        .show()
                }

        } else {
            binding.progressBar.visibility = View.GONE
        }
    }
}