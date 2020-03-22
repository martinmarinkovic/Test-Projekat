package com.martinmarinkovic.myapplication

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_image.view.*
import kotlinx.android.synthetic.main.note_image.view.*

class NoteImageAdapter(private val c: Context, private val images: ArrayList<String>) :
    RecyclerView.Adapter<NoteImageAdapter.ColorViewHolder>() {

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        return ColorViewHolder(LayoutInflater.from(c).inflate(R.layout.note_image, parent, false))
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val path = images[position]

        Glide.with(holder.iv.context)
            .load(path)
            //.override(250, 250)
            //.centerCrop()
            //.placeholder(R.drawable.ic_image_place_holder)
            .into(holder.iv );

        holder.iv .setOnClickListener {

            val recorderDialogView = LayoutInflater.from(holder.iv.context).inflate(R.layout.fragment_image, null)
            Picasso.get()
                .load(path)
                .into(recorderDialogView.image_view)
            val recorderDialogViewBuilder = AlertDialog.Builder(holder.iv.context).setView(recorderDialogView)
            val  recorderDialog = recorderDialogViewBuilder.show()
            recorderDialogView.btn_close.setOnClickListener{
                recorderDialog.dismiss()
            }
        }
    }

    class ColorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iv  = view.iv  as ImageView
    }

}