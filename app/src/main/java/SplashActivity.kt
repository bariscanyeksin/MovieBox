package com.yeksin.moviebox

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Durum çubuğunu saydam yapmak için aşağıdaki kodu ekleyin
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_blue)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.primary_blue)

        fetchApiKey()

        // Start the icon animation
        startIconAnimation()

        // Navigate to MainActivity after a delay
        navigateToMain()
    }

    private fun fetchApiKey() {
        val firestore = FirebaseFirestore.getInstance()
        val docRef = firestore.collection("api_key").document("X1OJWtqjmxXSkQBxWdyq")

        docRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val apiKey = document.getString("api_key")
                if (apiKey != null) {
                    Log.d("FirebaseActivity", "API Key: $apiKey")
                    // API anahtarını ApiService'e iletin
                    ApiService.updateApiKey(apiKey)
                } else {
                    Log.e("FirebaseActivity", "API Key exists but has null value")
                }
            } else {
                Log.e("FirebaseActivity", "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.e("FirebaseActivity", "Error fetching document", exception)
        }
    }


    private fun startIconAnimation() {
        val icon = findViewById<ImageView>(R.id.icon_image_view) // İkonu referans al
        val fadeInOut = AnimationUtils.loadAnimation(this, R.anim.fade_in_out)
        icon.startAnimation(fadeInOut)
        Log.d("SplashActivity", "Animation started")
    }

    private fun navigateToMain() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()  // SplashActivity'yi kapat
        }, 5000) // 5 saniye bekle
    }
}