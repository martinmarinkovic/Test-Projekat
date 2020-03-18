package com.martinmarinkovic.myapplication

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_reset_password.*

private lateinit var auth: FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        auth = FirebaseAuth.getInstance()

        btn_reset_password.setOnClickListener{

            var user_email = email.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()) {
                email.error = "Please enter valid email"
                email.requestFocus()
            } else {

                auth.sendPasswordResetEmail(user_email)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this@ResetPasswordActivity,
                                "We have sent you instructions to reset your password!",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        } else {
                            Toast.makeText(
                                baseContext,
                                "Failed to send reset email!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }

}
