package com.feritkeskin.chatapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.feritkeskin.chatapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    //<color name="buttonColor">#813fd7</color>

    private lateinit var binding: ActivityLoginBinding
    private var auth: FirebaseAuth? = null
    private var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firebaseUser = auth?.currentUser

        binding.girisYapButton.setOnClickListener {
            val email = binding.girisEmail.text.toString()
            val sifre = binding.girisParola.text.toString()
            if (sifre.isNullOrEmpty() && email.isNullOrEmpty()) {
                Toast.makeText(this, "Email ve Şifre boş bırakılamaz", Toast.LENGTH_SHORT).show()
            } else if (sifre.isNullOrEmpty()) {
                Toast.makeText(this, "Şifre boş bırakılamaz", Toast.LENGTH_SHORT).show()
            } else if (email.isNullOrEmpty()) {
                Toast.makeText(this, "Email boş bırakılamaz", Toast.LENGTH_SHORT).show()
            } else {
                auth?.signInWithEmailAndPassword(email, sifre)
                    ?.addOnCompleteListener(this) {
                        if (it.isSuccessful) {
                            Toast.makeText(this, "Giriş başarılı", Toast.LENGTH_SHORT).show()
                            binding.girisEmail.setText("")
                            binding.girisParola.setText("")
                            val intent = Intent(this@LoginActivity, UsersActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                    ?.addOnFailureListener {
                        Toast.makeText(this, "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        binding.girisYeniUyelik.setOnClickListener {
            val intent = Intent(this@LoginActivity, SingUpActivity::class.java)
            startActivity(intent)
        }
    }
}