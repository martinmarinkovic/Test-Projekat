package com.martinmarinkovic.myapplication

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_lock_screen.*
import kotlinx.android.synthetic.main.set_pin_dialog_layout.*

class LockScreenFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.fragment_lock_screen, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        btn_enable_lock_screen.setOnClickListener{
            startActivity(Intent(activity, TestLockScreenActivity::class.java))
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
