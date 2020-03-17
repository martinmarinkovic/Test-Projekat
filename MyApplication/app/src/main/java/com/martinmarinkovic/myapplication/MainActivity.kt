package com.martinmarinkovic.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()

        val isFirstRun = getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE)
                .getBoolean("isFirstRun", true)

        if (isFirstRun)
            Toast.makeText(this@MainActivity, "You need to login first!", Toast.LENGTH_LONG).show()

        getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE).edit()
            .putBoolean("isFirstRun", false).commit()

        tv_welcome.setText(getString(R.string.welcomeEng))

        if (auth != null)
            btn_main.setText(R.string.registered)
        else
            btn_main.setText(R.string.not_registered)

        btn_main.setOnClickListener{
            startActivity(Intent(this@MainActivity, SignInActivity::class.java))
        }
    }
}
