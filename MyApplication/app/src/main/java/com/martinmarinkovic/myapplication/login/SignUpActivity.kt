package com.martinmarinkovic.myapplication.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.martinmarinkovic.myapplication.R
import com.martinmarinkovic.myapplication.loading.LoadingAnimation
import com.martinmarinkovic.myapplication.loading.LoadingAsync
import com.martinmarinkovic.myapplication.loading.LoadingImplementation
import com.martinmarinkovic.myapplication.roomdb.User
import kotlinx.android.synthetic.main.activity_sign_in.btn_sign_up
import kotlinx.android.synthetic.main.activity_sign_in.tv_password
import kotlinx.android.synthetic.main.activity_sign_in.tv_username
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity(), LoadingImplementation {

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private lateinit var loadingAnimation : LoadingAnimation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        auth = FirebaseAuth.getInstance()

        btn_sign_up.setOnClickListener {
            signUpUser()
        }
    }

    private fun signUpUser() {
        if (tv_username.text.toString().isEmpty()) {
            tv_username.error = "Please enter username"
            tv_username.requestFocus()
            return
        }

        if (tv_email.text.toString().isEmpty()) {
            tv_email.error = "Please enter email"
            tv_email.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(tv_email.text.toString()).matches()) {
            tv_email.error = "Please enter valid email"
            tv_email.requestFocus()
            return
        }

        if (tv_password.text.toString().isEmpty()) {
            tv_password.error = "Please enter password"
            tv_password.requestFocus()
            return
        }

        auth.createUserWithEmailAndPassword(tv_email.text.toString(), tv_password.text.toString())
            .addOnCompleteListener(this) { task ->

                loadingAnimation =
                    LoadingAnimation(
                        this,
                        "loading.json"
                    )
                loadingAnimation.playAnimation(true)
                LoadingAsync(this).execute()

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.sendEmailVerification()
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                val uid = FirebaseAuth.getInstance().currentUser?.uid
                                val username = tv_username.text.toString()
                                if (uid != null) {
                                    val user = User(uid, username)
                                    db.collection("users").document(uid)
                                        .set(user)
                                        .addOnSuccessListener { documentReference ->
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(baseContext, "Adding user to db failed!", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        }
                } else {
                    onFinishedLoading()
                    Toast.makeText(baseContext, "Sign Up failed. Try again after some time.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onFinishedLoading() {
        //startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }
}