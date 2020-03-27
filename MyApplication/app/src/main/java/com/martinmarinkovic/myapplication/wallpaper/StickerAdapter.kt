package com.martinmarinkovic.myapplication.wallpaper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.martinmarinkovic.myapplication.R

/*
class StickerAdapter(private val c: Context, private val images: ArrayList<Int>) :
    RecyclerView.Adapter<StickerAdapter.ColorViewHolder>() {

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        return ColorViewHolder(
            LayoutInflater.from(c).inflate(R.layout.note_image, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        var path = images[position]

        Glide.with(holder.iv.context)
            .load(path)
            //.placeholder(R.drawable.ic_image_place_holder)
            .into(holder.iv)

        holder.iv.setOnClickListener {

        }
    }

    class ColorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iv  = view.iv  as ImageView
    }
}*/

class StickerAdapter(context: Context) : BaseAdapter() {

    private var items = arrayOfNulls<Int>(10)
    var inflater: LayoutInflater

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {

        var convertView = inflater.inflate(R.layout.sticker_layout, null)
        val imageView = convertView.findViewById<View>(R.id.iv) as ImageView
        imageView.setImageResource(items.get(position)!!)

        return convertView
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Int? {
        return items.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    init {
        items = arrayOfNulls(10)
        items[0] = R.drawable.sticker_1
        items[1] = R.drawable.sticker_2
        items[2] = R.drawable.sticker_3
        items[3] = R.drawable.sticker_4
        items[4] = R.drawable.sticker_5
        items[5] = R.drawable.sticker_6
        items[6] = R.drawable.sticker_7
        items[7] = R.drawable.sticker_8
        items[8] = R.drawable.sticker_9
        items[9] = R.drawable.sticker_10
        inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }
}