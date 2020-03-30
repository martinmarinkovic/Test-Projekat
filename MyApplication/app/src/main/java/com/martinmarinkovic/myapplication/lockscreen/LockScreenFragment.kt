package com.martinmarinkovic.myapplication.lockscreen

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.martinmarinkovic.myapplication.R
import kotlinx.android.synthetic.main.fragment_lock_screen.*

class LockScreenFragment : Fragment() {

    private var isServisActiv: Boolean = false;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.fragment_lock_screen, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        LockScreen.getInstance().init(context!!, true)

        if (LockScreen.getInstance().isActive()) {
            isServisActiv = true
            btn_enable_lock_screen.text = "Disable Lock Screen"
        } else {
            isServisActiv = false
            btn_enable_lock_screen.text = ("Enable Lock Screen")
        }

        btn_enable_lock_screen.setOnClickListener{
            if (!isServisActiv){
                LockScreen.getInstance().active()
            } else {
                LockScreen.getInstance().deactivate()
            }
        }

        btn_set_password.setOnClickListener{
            val mDialogView = LayoutInflater.from(activity).inflate(R.layout.set_pin_dialog_layout, null)
            val mBuilder = AlertDialog.Builder(activity)
                .setView(mDialogView)
            val  mAlertDialog = mBuilder.show()
            mAlertDialog.getWindow().setLayout(800, 800);
            mDialogView.setOnClickListener{
                mAlertDialog.dismiss()
            }
        }
    }
}
