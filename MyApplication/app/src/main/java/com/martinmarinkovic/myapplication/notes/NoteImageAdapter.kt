package com.martinmarinkovic.myapplication.notes

import android.app.AlertDialog
import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.martinmarinkovic.myapplication.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_image.view.*
import kotlinx.android.synthetic.main.note_image.view.*
import kotlinx.android.synthetic.main.play_audio_layout.view.*

class NoteImageAdapter(private val c: Context, private val images: ArrayList<String>, private var onFileCLickListener: OnFileCLickListener) :
    RecyclerView.Adapter<NoteImageAdapter.ColorViewHolder>() {

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        return ColorViewHolder(LayoutInflater.from(c).inflate(R.layout.note_image, parent, false), onFileCLickListener)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        var path = images[position]

        if (path.contains(".mp3")){
            Glide.with(holder.iv.context)
                .load(R.drawable.note_layout_image)
                .into(holder.iv);
        } else {
            Glide.with(holder.iv.context)
                .load(path)
                .into(holder.iv );
        }
    }

    class ColorViewHolder(view: View, var onFileCLickListener: OnFileCLickListener) : RecyclerView.ViewHolder(view), View.OnClickListener {

        val iv  = view.iv  as ImageView
        override fun onClick(v: View?) {
            onFileCLickListener.onFileClick(adapterPosition)
        }
        init {
            view.setOnClickListener(this)
        }
    }

    interface OnFileCLickListener{
        fun onFileClick(position: Int)
    }
}