package com.martinmarinkovic.myapplication.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.martinmarinkovic.myapplication.NavigationActivity
import com.martinmarinkovic.myapplication.R
import com.martinmarinkovic.myapplication.helper.toast
import com.martinmarinkovic.myapplication.loading.LoadingAnimation
import com.martinmarinkovic.myapplication.loading.LoadingAsync
import com.martinmarinkovic.myapplication.loading.LoadingImplementation
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity(), LoadingImplementation {

    private lateinit var auth: FirebaseAuth
    private lateinit var loadingAnimation : LoadingAnimation
    private var check: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        auth = FirebaseAuth.getInstance()

        btn_sign_up.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        btn_reset_password.setOnClickListener{
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }

        btn_log_in.setOnClickListener {
            doLogin()
        }
    }

    private fun doLogin() {
        if (tv_username.text.toString().isEmpty()) {
            tv_username.error = "Please enter email"
            tv_username.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(tv_username.text.toString()).matches()) {
            tv_username.error = "Please enter valid email"
            tv_username.requestFocus()
            return
        }

        if (tv_password.text.toString().isEmpty()) {
            tv_password.error = "Please enter password"
            tv_password.requestFocus()
            return
        }

        auth.signInWithEmailAndPassword(tv_username.text.toString(), tv_password.text.toString())
            .addOnCompleteListener(this) { task ->

                loadingAnimation =
                    LoadingAnimation(
                        this,
                        "loading1.json"
                    )
                loadingAnimation.playAnimation(true)
                LoadingAsync(this).execute()

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateUI(user)
                } else {

                    updateUI(null)
                }
            }.addOnFailureListener{
                toast("Login failed")
            }
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {

        if (currentUser != null) {
            if(currentUser.isEmailVerified) {
                check = true
            }else{
                check = false
                toast("Please verify your email address")
            }
        }
    }

    override fun onFinishedLoading() {
        if (check) {
            startActivity(Intent(this, NavigationActivity::class.java))
            finish()
        } else
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
    }
}