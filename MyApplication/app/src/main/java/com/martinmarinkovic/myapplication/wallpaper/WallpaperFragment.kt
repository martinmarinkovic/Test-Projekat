package com.martinmarinkovic.myapplication.wallpaper

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.navigation.Navigation
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.martinmarinkovic.myapplication.R
import com.martinmarinkovic.myapplication.helper.toast
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.fragment_wallpaper.*

class WallpaperFragment : Fragment() {

    private val TAKE_PHOTO_REQUEST: Int = 2
    private val PICK_PHOTO_REQUEST: Int = 1
    private var fileUri: Uri? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_wallpaper, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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
        val values = ContentValues(1)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        fileUri = activity!!.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(intent.resolveActivity(activity!!.packageManager) != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
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
            CropImage.activity(fileUri)
                .setMinCropWindowSize(500, 1000)
                .setAspectRatio(9,19)
                .start(context!!,this)
        }
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_PHOTO_REQUEST) {
            fileUri = data?.data
            CropImage.activity(fileUri)
                .setMinCropWindowSize(500, 1000)
                .setAspectRatio(9,19)
                .start(context!!,this)
        }
        if (requestCode === CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            var result = CropImage.getActivityResult(data)
            if (resultCode === Activity.RESULT_OK) {
                val resultUri = result.uri

                val action =
                    WallpaperFragmentDirections.actionAddWallpaper()
                action.string = resultUri.toString()
                Navigation.findNavController(view!!).navigate(action)

            } else if (resultCode === CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
                activity?.toast("Error:$result.error")
        }
    }
}
