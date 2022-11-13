package com.feritkeskin.chatapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.feritkeskin.chatapp.databinding.ActivitySingUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SingUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySingUpBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingUpBinding.inflate(layoutInflater)
        mAuth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        uyeGiris()
        setContentView(binding.root)
    }

    private fun uyeGiris() {
        binding.uyeKaydetButton.setOnClickListener {
            val uyeAdSoyad = binding.uyeAdSoyad.text.toString()
            val email = binding.uyeEmail.text.toString()
            val sifre = binding.uyeParola.text.toString()
            if (sifre.isEmpty() && email.isEmpty() && uyeAdSoyad.isEmpty()) {
                Toast.makeText(this, "Kullanıcı bilgileri boş bırakılamaz", Toast.LENGTH_SHORT)
                    .show()

            } else if (sifre.isEmpty()) {
                Toast.makeText(this, "Şifre boş bırakılamaz", Toast.LENGTH_SHORT).show()
            } else if (email.isEmpty()) {
                Toast.makeText(this, "E-mail boş bırakılamaz", Toast.LENGTH_SHORT).show()
            } else if (uyeAdSoyad.isEmpty()) {
                Toast.makeText(this, "Üye adı Soyadı boş bırakılamaz", Toast.LENGTH_SHORT).show()
            } else {
                mAuth.createUserWithEmailAndPassword(email, sifre)
                    .addOnSuccessListener { uuid ->
                        println("Not user info: ${uuid.user?.uid.orEmpty()}, $uyeAdSoyad")
                        val hashMap: HashMap<String, String> = HashMap()
                        hashMap.put("userId", uuid.user?.uid.orEmpty())//girisYeniUyelik.toString()
                        hashMap.put("userName", uyeAdSoyad)
                        hashMap.put("profileImage", "")

                        database.child("users").child(uuid.user?.uid.orEmpty()).setValue(hashMap)
                            .addOnSuccessListener {
                                val intent = Intent(this@SingUpActivity, UsersActivity::class.java)
                                startActivity(intent)
                                Toast.makeText(
                                    this,
                                    "Kayıt başarılı, Hoşgeldin",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                    }

                    .addOnFailureListener {
                        Toast.makeText(this, "Kayıt hatalı ${it.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }
    }
}