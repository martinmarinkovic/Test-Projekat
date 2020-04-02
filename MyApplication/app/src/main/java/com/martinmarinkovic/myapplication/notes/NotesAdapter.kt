package com.martinmarinkovic.myapplication.notes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.martinmarinkovic.myapplication.R
import com.martinmarinkovic.myapplication.roomdb.Note
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_image.view.*
import kotlinx.android.synthetic.main.note_layout.view.*

class NotesAdapter(private val notes: List<Note>) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.note_layout,
                    parent,
                    false
                )
        )
    }

    override fun getItemCount() = notes.size

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.view.text_view_title.text = notes[position].title
        holder.view.text_view_note.text = notes[position].note

        if (notes[position].images!!.isNotEmpty()){
            var image = notes[position].images?.get(0)
            Glide.with(holder.view.iv_image.context)
                .load(image)
                .into(holder.view.iv_image)
        } else if (notes[position].audioFiles!!.isNotEmpty()){
            Glide.with(holder.view.iv_image.context)
                .load(R.drawable.note_layout_image)
                .into(holder.view.iv_image)
        }
        holder.view.setOnClickListener {
            val action =
                NotesFragmentDirections.actionAddNote()
            action.note = notes[position]
            Navigation.findNavController(it).navigate(action)
        }
    }

    class NoteViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}