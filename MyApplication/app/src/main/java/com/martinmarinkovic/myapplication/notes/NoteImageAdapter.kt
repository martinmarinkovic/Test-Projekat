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

private lateinit var mediaPlayer: MediaPlayer

class NoteImageAdapter(private val c: Context, private val images: ArrayList<String>) :
    RecyclerView.Adapter<NoteImageAdapter.ColorViewHolder>() {

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        return ColorViewHolder(
            LayoutInflater.from(c).inflate(
                R.layout.note_image,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        var path = images[position]

        if (path.contains(".mp3")){
            Glide.with(holder.iv.context)
                .load(R.drawable.note_layout_image)
                //.placeholder(R.drawable.ic_image_place_holder)
                .into(holder.iv );

            holder.iv .setOnClickListener {

                val recorderDialogView = LayoutInflater.from(holder.iv.context).inflate(
                    R.layout.play_audio_layout, null)
                val recorderDialogViewBuilder = AlertDialog.Builder(holder.iv.context).setView(recorderDialogView)
                val  recorderDialog = recorderDialogViewBuilder.show()
                recorderDialog.getWindow().setLayout(400, 400);
                recorderDialogView.btn_play.setOnClickListener{
                    mediaPlayer = MediaPlayer()
                    mediaPlayer.setDataSource(path)
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                }
            }
        } else {
            Glide.with(holder.iv.context)
                .load(path)
                .into(holder.iv );

            holder.iv .setOnClickListener {

                val imageDialogView = LayoutInflater.from(holder.iv.context).inflate(
                    R.layout.fragment_image, null)
                Glide.with(imageDialogView.image_view.context)
                    .load(path)
                    .into(imageDialogView.image_view)
                val imageDialogViewBuilder = AlertDialog.Builder(holder.iv.context).setView(imageDialogView)
                val  imageDialog = imageDialogViewBuilder.show()
                imageDialogView.btn_close.setOnClickListener{
                    imageDialog.dismiss()
                }
            }
        }
    }

    class ColorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iv  = view.iv  as ImageView
    }
}