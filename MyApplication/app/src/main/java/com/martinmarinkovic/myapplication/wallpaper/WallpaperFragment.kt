package com.martinmarinkovic.myapplication.wallpaper

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.view.*
import androidx.fragment.app.Fragment
import androidx.annotation.RequiresApi
import androidx.navigation.Navigation
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.martinmarinkovic.myapplication.R
import com.martinmarinkovic.myapplication.helper.toast
import com.martinmarinkovic.myapplication.notes.AddNoteFragment
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.fragment_wallpaper.*
import java.io.File
import java.util.*

class WallpaperFragment : Fragment() {

    private val TAKE_PHOTO_REQUEST: Int = 2
    private val PICK_PHOTO_REQUEST: Int = 1
    private var fileUri: Uri? = null
    private var path: File? = null
    private val MAX_LENGTH = 10
    private var imageCamera: String? = null
    private var screen_width = 0
    private var screen_height = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_wallpaper, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getScreenSize()

        btn_gallery.setOnClickListener {
            pickPhotoFromGallery()
        }

        btn_camera.setOnClickListener {
            askCameraPermission()
        }
    }

    private fun pickPhotoFromGallery() {
        val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickImageIntent, PICK_PHOTO_REQUEST)
    }

    private fun launchCamera() {
        val builder: StrictMode.VmPolicy.Builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val root = Environment.getExternalStorageDirectory().toString()
        val myDir = File("$root/MyApp/camera")
        myDir.mkdirs()
        path = File(myDir.toString() + "/" + getRandomString() + ".jpg")
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(intent.resolveActivity(activity!!.packageManager) != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(path))
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivityForResult(intent, TAKE_PHOTO_REQUEST)
        }
    }

    fun askCameraPermission(){
        Dexter.withActivity(activity).withPermissions(
            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport) {/* ... */
                if(report.areAllPermissionsGranted()){
                    launchCamera()
                }else{
                }
            }
            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?, token: PermissionToken?
            ) { context?.let {
                AlertDialog.Builder(it)
                    .setTitle("Permissions Error!").setMessage("Please allow permissions to take photo with camera")
                    .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                        dialog.dismiss()
                        token?.cancelPermissionRequest()
                    }
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->
                        dialog.dismiss()
                        token?.continuePermissionRequest()
                    }
                    .setOnDismissListener { token?.cancelPermissionRequest() }
                    .show()
                }
            }
        }).check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == TAKE_PHOTO_REQUEST) {
            imageCamera = path?.absolutePath.toString()
            CropImage.activity(Uri.fromFile(path))
                .setMinCropWindowSize(screen_width/2, screen_height/2)
                .setAspectRatio(9,16)
                .start(context!!,this)
        }
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_PHOTO_REQUEST) {
            fileUri = data?.data
            CropImage.activity(fileUri)
                .setMinCropWindowSize(screen_width/2, screen_height/2)
                .setAspectRatio(9,16)
                .start(context!!,this)
        }
        if (requestCode === CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            var result = CropImage.getActivityResult(data)
            if (resultCode === Activity.RESULT_OK) {
                val resultUri = result.uri
                if (imageCamera != null){
                    val deleteFromInternalStorage = File(imageCamera)
                    deleteFromInternalStorage?.delete()
                }
                val action = WallpaperFragmentDirections.actionAddWallpaper()
                action.string = resultUri.toString()
                Navigation.findNavController(view!!).navigate(action)

            } else if (resultCode === CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
                activity?.toast("Error:$result.error")
        }
    }

    private fun getScreenSize() {
        val display: Display = activity?.windowManager!!.defaultDisplay;
        val size = Point()
        display.getSize(size)
        screen_width = size.x
        screen_height = size.y
    }

    companion object {
        private const val ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm"
    }

    private fun getRandomString(): String {
        val random = Random()
        val sb = StringBuilder(MAX_LENGTH)
        for (i in 0 until MAX_LENGTH)
            sb.append(ALLOWED_CHARACTERS[random.nextInt(ALLOWED_CHARACTERS.length)])
        return sb.toString()
    }
}
