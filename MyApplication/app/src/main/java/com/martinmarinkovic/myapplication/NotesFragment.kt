package com.martinmarinkovic.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_notes.*

class NotesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_add.setOnClickListener {

          /*  val addNoteFragment: Fragment = AddNoteFragment()
            val fragmentManager: FragmentManager = activity!!.supportFragmentManager
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(android.R.id.content, addNoteFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()*/

            val action = NotesFragmentDirections.actionAddNote()
            Navigation.findNavController(it).navigate(action)

        }
    }
}
