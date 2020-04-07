package com.martinmarinkovic.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.martinmarinkovic.myapplication.roomdb.User
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.android.synthetic.main.nav_header_navigation.*

class NavigationActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener{

    private lateinit var appBarConfiguration: AppBarConfiguration
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_notes, /*R.id.addNoteFragment,*/ R.id.nav_wallpaper, R.id.nav_lock_screen, R.id.nav_user_settings, R.id.nav_developer_page, R.id.nav_bug_report
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(this)
        setUsername()
    }

    private fun setUsername() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            var ref = db.collection("users").document(firebaseUser.uid)
            ref.get().addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject<User>()
                if (user != null) {
                    tv_username.text = user.username
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

   override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_notes -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.nav_notes)
            }
            R.id.nav_wallpaper -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.nav_wallpaper)
            }
            R.id.nav_lock_screen -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.nav_lock_screen)
            }
            R.id.nav_user_settings -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.nav_user_settings)
            }
            R.id.nav_developer_page -> {
                startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/dev?id=6636036929415414830")))
            }
            R.id.nav_bug_report -> {
                startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("mailto:" + getString(R.string.report_mail))))
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
