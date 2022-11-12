package com.feritkeskin.chatapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.feritkeskin.chatapp.databinding.ActivityDeleteAccountBinding
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class DeleteAccountActivity : AppCompatActivity() {

    lateinit var binding: ActivityDeleteAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
        val view = binding.root

        binding.deleteButton.setOnClickListener {

            if (binding.emailDelete.text == null || binding.passwordDelete.text == null) {
                Toast.makeText(
                    this@DeleteAccountActivity,
                    "E-mail veya Şifre boş bırakılamaz!!",
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                //Alert Uyarı Bildirimi
                val alert = android.app.AlertDialog.Builder(this)
                alert.setTitle("Hesabımı Kalıcı Olarak Sil")
                alert.setMessage("Hesabını Silmek İstediğine Emin Misin?")
                alert.setPositiveButton("EVET") { dialog, which ->
                    deleteAccount()
                }
                alert.setNegativeButton("HAYIR") { dialog, which ->
                    Toast.makeText(
                        applicationContext,
                        "Hesap Silme Başarısız!!",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                alert.show()
            }
        }
        setContentView(view)
    }

    private fun deleteAccount() {

        val deleteEmail = binding.emailDelete.text.toString()
        val deletePassword = binding.passwordDelete.text.toString()

        if (deletePassword.isNullOrEmpty() && deleteEmail.isNullOrEmpty()) {
            Toast.makeText(this, "E-mail ve Şifre boş bırakılamaz", Toast.LENGTH_SHORT).show()
        } else if (deletePassword.isNullOrEmpty()) {
            Toast.makeText(this, "Şifre boş bırakılamaz", Toast.LENGTH_SHORT).show()
        } else if (deleteEmail.isNullOrEmpty()) {
            Toast.makeText(this, "E-mail boş bırakılamaz", Toast.LENGTH_SHORT).show()
        } else {

            val user = FirebaseAuth.getInstance().currentUser
            val credential: AuthCredential =
                EmailAuthProvider.getCredential(deleteEmail, deletePassword)
            user?.reauthenticate(credential)?.addOnCompleteListener {
                if (it.isSuccessful) {
                    user.delete()
                    Toast.makeText(this, "Hesap silindi.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@DeleteAccountActivity, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }?.addOnFailureListener {
                Toast.makeText(this, "Hesap bilgileri yanlış.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}