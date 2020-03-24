package com.martinmarinkovic.myapplication.wallpaper

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.martinmarinkovic.myapplication.R
import com.martinmarinkovic.myapplication.helper.toast
import kotlinx.android.synthetic.main.fragment_add_wallpaper.*

class AddWallpaperFragment : Fragment() {

    var bitmap: Bitmap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_wallpaper, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let {
            val image = AddWallpaperFragmentArgs.fromBundle(
                it
            ).string
            val imageUri = Uri.parse(image)
            Glide.with(activity!!)
                .load(imageUri)
                .into(image_view )
            bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, imageUri)
        }

        btn_set_wallpaper.setOnClickListener{
            var wallpaperManager: WallpaperManager = WallpaperManager.getInstance(activity)
            wallpaperManager.setBitmap(bitmap)
            activity?.toast("New wallpaper is set")
        }

        btn_stickers.setOnClickListener{

        }
    }

}
