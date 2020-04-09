package com.martinmarinkovic.myapplication.lockscreen

import android.app.Activity.RESULT_OK
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.martinmarinkovic.myapplication.R
import com.martinmarinkovic.myapplication.helper.toast
import kotlinx.android.synthetic.main.fragment_lock_screen.*

class LockScreenFragment : Fragment() {

    private val PACKAGE_NAME = "com.martinmarinkovic.myapplication"
    private val PIN_SAVED = "pinEnabled"
    private val REQUEST_CODE = 123
    private val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5469

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.fragment_lock_screen, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        LockScreen.getInstance().init(context!!, true)
        check()


        btn_enable_lock_screen.setOnClickListener{
            LockScreen.getInstance().init(context!!, true)
            if (!LockScreen.getInstance().isActive()){
                if (!Settings.canDrawOverlays(context)) {
                    requestPermission()
                } else if (Settings.canDrawOverlays(context)) {
                    LockScreen.getInstance().active()
                }
                btn_enable_lock_screen.text = "Disable Lock Screen"
                btn_set_password.isEnabled = true
                btn_set_password.setTextColor(Color.WHITE)
            } else {
                LockScreen.getInstance().deactivate()
                btn_enable_lock_screen.text = ("Enable Lock Screen")
                btn_set_password.setTextColor(Color.GRAY)
                btn_set_password.isEnabled = false
            }
        }

        btn_set_password.setOnClickListener{

            val intent = LockScreenActivity.getIntent(context, true)
            startActivityForResult(intent, REQUEST_CODE)

            //val intent = Intent(context, EnterPinActivity::class.java)
            //startActivity(intent)
            /*val mDialogView = LayoutInflater.from(activity).inflate(R.layout.set_pin_dialog_layout, null)
            val mBuilder = AlertDialog.Builder(activity)
                .setView(mDialogView)
            val  mAlertDialog = mBuilder.show()
            mAlertDialog.getWindow().setLayout(800, 800);
            mDialogView.setOnClickListener{
                mAlertDialog.dismiss()
            }*/
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= 26) {
            if (Settings.canDrawOverlays(context)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$PACKAGE_NAME"))
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE ->
                if (resultCode == RESULT_OK) {
                val editor: SharedPreferences.Editor = activity?.getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE)!!
                    .edit()
                    editor.putBoolean(PIN_SAVED, true)
                    editor.apply()
                    activity?.toast(getString(R.string.pin_enabled))
            }
            ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE->
                if (Build.VERSION.SDK_INT >= 26){
                    if (Settings.canDrawOverlays(context)) {
                        LockScreen.getInstance().active()
                    }
                }
        }
    }

    private fun check(){
        if (!LockScreen.getInstance().isActive()) {
            btn_enable_lock_screen.text = "Enable Lock Screen"
            btn_set_password.isEnabled = false
            btn_set_password.setTextColor(Color.GRAY)
        } else {
            btn_enable_lock_screen.text = ("Disable Lock Screen")
            btn_set_password.setTextColor(Color.WHITE)
            btn_set_password.isEnabled = true
        }
    }
}
