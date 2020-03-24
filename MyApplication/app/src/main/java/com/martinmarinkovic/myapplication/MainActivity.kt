package com.martinmarinkovic.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.martinmarinkovic.myapplication.loading.LoadingAnimation
import com.martinmarinkovic.myapplication.loading.LoadingAsync
import com.martinmarinkovic.myapplication.loading.LoadingImplementation
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(),
    LoadingImplementation {

    override fun onFinishedLoading() {
        loadingAnimation.stopAnimation(R.layout.activity_main)

        val isFirstRun = getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE)
            .getBoolean("isFirstRun", true)

        if (isFirstRun)
            Toast.makeText(this@MainActivity, "You need to login first!", Toast.LENGTH_LONG).show()

        getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE).edit()
            .putBoolean("isFirstRun", false).commit()

        if (signin) {
            tv_welcome.setText(getString(R.string.welcome))
            tv_username.setText(username)
            btn_main.setText(R.string.registered)
        } else {
            btn_main.setText(R.string.not_registered)
        }

        btn_main.setOnClickListener{
            startActivity(Intent(this@MainActivity, SignInActivity::class.java))
        }
    }

    private lateinit var loadingAnimation : LoadingAnimation
    private val db = Firebase.firestore
    private val firebaseUser = FirebaseAuth.getInstance().currentUser
    private var username: String? = null
    private var signin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        loadingAnimation =
            LoadingAnimation(
                this,
                "loading.json"
            )
        loadingAnimation.playAnimation(true)
        LoadingAsync(this).execute()

        if (firebaseUser != null) {
            var ref = db.collection("users").document(firebaseUser.uid)
            ref.get().addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject<User>()
                if (user != null) {
                    signin = true
                    username = user.username
                }
            }
        }
    }
}
