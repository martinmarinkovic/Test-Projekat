package com.martinmarinkovic.myapplication.wallpaper

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.GridView
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.martinmarinkovic.myapplication.R
import com.martinmarinkovic.myapplication.helper.toast
import com.martinmarinkovic.myapplication.wallpaper.sticker.StickerImage
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.fragment_add_wallpaper.*
import java.io.File
import java.io.FileOutputStream
import java.util.*


class AddWallpaperFragment : Fragment() {

    private val NONE = 0
    private val FRAME = 3
    private val SCROLL = 4
    private val DRAG = 5
    private var mode = NONE
    private var start:Point = Point()
    lateinit var canvasWidth: Any
    lateinit var canvasHeight: Any
    private var _DecorationsSaved = true
    private var _GalleryStickersOn = false
    private var _StickersGallery: GridView? = null
    private var _StickersButton: Button? = null
    private var _ContainerLayout: RelativeLayout? = null
    private var StickersLayout: RelativeLayout? = null
    private var _IdOfSelectedView = 0
    private var _CurrentView = 0
    private var _ViewsCount = 0
    private var _AddWallpaperFragment: AddWallpaperFragment? = null
    private var imageUri: Uri? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_add_wallpaper, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        StickersLayout = activity?.findViewById(R.id.relative_layout_for_stickers)
        _ContainerLayout = activity?.findViewById(R.id.relative_layout_for_stickersAndPhoto)
        _StickersButton = activity?.findViewById(R.id.button_stickers)
        _StickersGallery = activity?.findViewById(R.id.gridview_StickersGallery)
        _StickersGallery!!.adapter = activity?.let { StickerAdapter(it) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let {
            val image = AddWallpaperFragmentArgs.fromBundle(
                it
            ).string
            imageUri = Uri.parse(image)
            Glide.with(activity!!)
                .load(imageUri)
                .into(image_view )
        }

        _AddWallpaperFragment = this
        canvasWidth = StickersLayout?.layoutParams!!.width
        canvasHeight = StickersLayout?.layoutParams!!.height

        btn_set_wallpaper.setOnClickListener{
            saveWallpaper()
        }

        _StickersButton?.setOnClickListener{
            if (!_GalleryStickersOn) {
                _StickersGallery!!.visibility = View.VISIBLE
                _GalleryStickersOn = true
            } else {
                _StickersGallery!!.visibility = View.INVISIBLE
                _GalleryStickersOn = false
            }
        }

        _StickersGallery!!.visibility = View.INVISIBLE
        _StickersGallery!!.onItemClickListener = OnItemClickListener { parent, view, position, id ->

            val img = StickerImage(activity, "sticker_" + (position + 1).toString(), _ViewsCount, _AddWallpaperFragment!!, 0, 0)
            StickersLayout?.addView(img)
            _ViewsArray.add(img)
            _ViewsCount++
            img.invalidate()
            _StickersGallery!!.visibility = View.INVISIBLE
            _GalleryStickersOn = false
        }
    }

    private fun saveWallpaper() {
        StickersLayout?.invalidate()
        val img = setViewToBitmapImage(StickersLayout!!)
        if (img != null) {
            var wallpaperManager: WallpaperManager = WallpaperManager.getInstance(activity)
            wallpaperManager.setBitmap(img)
            SaveImage(img)
            activity?.toast("New wallpaper is set")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> deleteSticker()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.add_wallpaper_menu, menu)
    }

    private fun deleteSticker() {
        if (_ViewsArray.size > 0) {
            if (_CurrentView != -1) {
                Log.i("DELETE", "Should remove this view")
                StickersLayout?.removeView(
                    _ViewsArray[_CurrentView]
                )
                _ViewsArray.removeAt(_CurrentView)
                shiftAllElements(_CurrentView)
                _ViewsCount -= 1
                _CurrentView = -1
            }
        }
    }

    private fun deleteAllStickers() {
        _DecorationsSaved = false
        if (_ViewsArray.size > 0) {
            StickersLayout?.removeAllViews()
            _ViewsArray.clear()
            _ViewsCount = 0
            _CurrentView = -1
        } else {
            Log.i("DELETE", "You dont have items for delete")
        }
    }

    private fun shiftAllElements(idRemovedSticker: Int) {
        for (i in idRemovedSticker until _ViewsArray.size) {
            if (_ViewsArray[i] is StickerImage) {
                (_ViewsArray[i] as StickerImage).set_NumberView(
                    i
                )
            }
        }
    }

    fun setmCurrentView(mCurrentView: Int) {
        _CurrentView = mCurrentView
    }

    private fun setViewToBitmapImage(view: View): Bitmap? {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null)
            bgDrawable.draw(canvas)
        else
            canvas.drawColor(Color.BLACK)
        view.draw(canvas)
        return returnedBitmap
    }

    private fun SaveImage(finalBitmap: Bitmap) {
        val root = Environment.getExternalStorageDirectory().toString()
        val myDir = File("$root/MyApp/saved_images")
        myDir.mkdirs()
        val generator = Random()
        var n = 10000
        n = generator.nextInt(n)
        val fname = "Image-$n.jpg"
        val file = File(myDir, fname)
        if (file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        @kotlin.jvm.JvmField
        var _IdOfSelectedView: Int = 0
        private var _ViewsArray = ArrayList<View>()

        @JvmStatic
        fun invalidateOtherStickers(currentStickerId: Int) {
            for (i in currentStickerId - 1 downTo 0) {
                if (_ViewsArray[i] is StickerImage) {
                    (_ViewsArray[i] as StickerImage).set_Selected(
                        false
                    )
                    _ViewsArray[i].invalidate()
                }
            }
        }
    }

}
