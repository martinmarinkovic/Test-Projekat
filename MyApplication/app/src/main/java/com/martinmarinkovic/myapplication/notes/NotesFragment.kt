package com.martinmarinkovic.myapplication.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.martinmarinkovic.myapplication.R
import com.martinmarinkovic.myapplication.roomdb.NoteDatabase
import kotlinx.android.synthetic.main.fragment_notes.*
import kotlinx.coroutines.launch

class NotesFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_notes, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recycler_view_notes.setHasFixedSize(true)
        recycler_view_notes.layoutManager =
            LinearLayoutManager(activity)

        launch {
            context?.let{
                val notes = NoteDatabase(it).getNoteDao().getAllNotes()
                recycler_view_notes.adapter =
                    NotesAdapter(notes)
            }
        }

        button_add.setOnClickListener {
            val action =
                NotesFragmentDirections.actionAddNote()
            Navigation.findNavController(it).navigate(action)
        }
    }
}
