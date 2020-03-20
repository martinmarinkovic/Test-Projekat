package com.martinmarinkovic.myapplication

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.Navigation
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.theartofdev.edmodo.cropper.CropImage


class PopUpFragment : DialogFragment() {

    val TAKE_PHOTO_REQUEST: Int = 2
    val PICK_PHOTO_REQUEST: Int = 1
    var fileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView: View = inflater.inflate(R.layout.fragment_pop_up, container, false)

        var btnGallery = rootView.findViewById<LinearLayout>(R.id.btn_gallery)
        var btnCamera = rootView.findViewById<LinearLayout>(R.id.btn_camera)
        var btnAudio = rootView.findViewById<LinearLayout>(R.id.btn_audio)

        btnGallery.setOnClickListener(object: View.OnClickListener
        {
            override fun onClick(v: View?) {
                pickPhotoFromGallery()
            }
        }
    )

        btnCamera.setOnClickListener{
            askCameraPermission()
        }

        btnAudio.setOnClickListener{

        }

        return rootView
    }

    /*override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        btn_gallery.setOnClickListener{
            pickPhotoFromGallery()
        }

        btn_camera.setOnClickListener{
            askCameraPermission()
        }

        btn_audio.setOnClickListener{

        }
    }*/

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
        Dexter.withActivity(activity).withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {/* ... */
                    if(report.areAllPermissionsGranted()){
                        launchCamera()
                    }else{
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?, token: PermissionToken?
                ) { context?.let {
                        AlertDialog.Builder(it)
                            .setTitle(
                                "Permissions Error!")
                            .setMessage(
                                "Please allow permissions to take photo with camera")
                            .setNegativeButton(
                                android.R.string.cancel,
                                { dialog, _ ->
                                    dialog.dismiss()
                                    token?.cancelPermissionRequest()
                                })
                            .setPositiveButton(android.R.string.ok,
                                { dialog, _ ->
                                    dialog.dismiss()
                                    token?.continuePermissionRequest()
                                })
                            .setOnDismissListener({
                                token?.cancelPermissionRequest() })
                            .show()
                    }
                }
            }).check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && requestCode == TAKE_PHOTO_REQUEST) {
            //imageView.setImageURI(fileUri)
        } else if(resultCode == RESULT_OK && requestCode == PICK_PHOTO_REQUEST){
            fileUri = data?.data
            CropImage.activity(fileUri)
                //.setMinCropWindowSize(500, 500)
                .start(activity!!)
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            Toast.makeText(activity, "UPAOOOO",Toast.LENGTH_SHORT).show()
            if (resultCode == RESULT_OK) {
                var resultUri = result.getUri();

                val action = PopUpFragmentDirections.actionAddFileToNote()
                view?.let { Navigation.findNavController(it).navigate(action) }

                Toast.makeText(activity, "UPAOOOO i doleeeee!!!!",Toast.LENGTH_SHORT).show()
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
