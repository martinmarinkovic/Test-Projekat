package com.martinmarinkovic.myapplication.wallpaper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.martinmarinkovic.myapplication.R

class StickerAdapter(context: Context) : BaseAdapter() {

    private var mContext: Context? = null
    private var items = arrayOfNulls<Int>(10)
    private var inflater: LayoutInflater? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var view = convertView
        view = inflater!!.inflate(R.layout.note_image, null)
        val imageView = view?.findViewById<View>(R.id.iv) as ImageView
        imageView.setImageResource(items[position]!!)
        return view
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position]!!
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    init {
        mContext = context
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
        inflater = mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }
}