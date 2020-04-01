package com.martinmarinkovic.myapplication.notes

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.martinmarinkovic.myapplication.*
import com.martinmarinkovic.myapplication.helper.toast
import com.martinmarinkovic.myapplication.roomdb.Note
import com.martinmarinkovic.myapplication.roomdb.NoteDatabase
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.audio_recorder_layout.view.*
import kotlinx.android.synthetic.main.custom_dialog_layout.view.*
import kotlinx.android.synthetic.main.fragment_add_note.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class AddNoteFragment : BaseFragment() {

    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    private var uid: String? = null
    private var note: Note? = null
    private val db = Firebase.firestore
    private val TAKE_PHOTO_REQUEST: Int = 2
    private val PICK_PHOTO_REQUEST: Int = 1
    private var fileUri: Uri? = null
    private var imgUri: Uri? = null
    private var image: String? = null
    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private val MAX_LENGTH = 10
    private var imageListToShow: ArrayList<String> = ArrayList()
    private var imageListToUpload: ArrayList<String> = ArrayList()
    private var noteId: String = ""
    private var audioFilesList: ArrayList<String> = ArrayList()
    private var allFilesList: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_add_note, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        uid = FirebaseAuth.getInstance().currentUser?.uid

        arguments?.let {
            note = AddNoteFragmentArgs.fromBundle(
                it
            ).note
            edit_text_title.setText(note?.title)
            edit_text_note.setText(note?.note)
            if (note?.id != null)
                noteId = note?.id!!
            if(note?.images != null) {
                imageListToShow = note?.images!!
                allFilesList.addAll(imageListToShow)
                setImages(allFilesList)
            }
            if (note?.audioFiles != null) {
                audioFilesList = note?.audioFiles!!
                allFilesList.addAll(audioFilesList)
                setImages(allFilesList)
            }
        }
    }

    private fun save() {
        val noteTitle = edit_text_title.text.toString().trim()
        val noteBody = edit_text_note.text.toString().trim()

        if (noteTitle.isEmpty()) {
            edit_text_title.error = "title required"
            edit_text_title.requestFocus()
            return
        }

        if (noteBody.isEmpty()) {
            edit_text_note.error = "note required"
            edit_text_note.requestFocus()
            return
        }

        launch {

            context?.let {
                val mNote = Note(noteId, noteTitle, noteBody, imageListToShow, audioFilesList)

                if (note == null) {
                    noteId = getRandomString()
                    mNote.id = noteId
                    NoteDatabase(it).getNoteDao().addNote(mNote)
                    saveNote(mNote)
                    if (imageListToUpload.isNotEmpty())
                        uploadImage(mNote)
                } else {
                    //mNote.id = note!!.id
                    NoteDatabase(it).getNoteDao().updateNote(mNote)
                    updateNote(mNote)
                    if (imageListToUpload.isNotEmpty())
                        uploadImage(mNote)
                }

                val action =
                    AddNoteFragmentDirections.actionSaveNote()
                view?.let { it1 -> Navigation.findNavController(it1).navigate(action) }
            }
        }
    }

    private fun setImages(list: ArrayList<String>){
        val sglm = StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL)
        rv.layoutManager = sglm
        val igka =
            NoteImageAdapter(
                activity!!,
                list
            )
        rv.adapter = igka
    }


    private fun saveNote(note: Note) {
        if (uid != null) {
            var ref = db.collection("users").document(uid!!)
            ref.get().addOnSuccessListener {
                db.collection("users").document(uid!!).collection("notes").document(note.id!!)
                    .set(note)
                    .addOnSuccessListener {
                        Toast.makeText(activity, "Success!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(activity, "Fail!", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun deleteNoteFirestore(note: Note) {
        if (uid != null) {
            var ref = db.collection("users").document(uid!!)
            ref.get().addOnSuccessListener {
                db.collection("users").document(uid!!).collection("notes").document(note.id!!)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(activity, "Success!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(activity, "Fail!", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun updateNote(note: Note){
        db.collection("users").document(uid!!).collection("notes").document(note.id!!)
            .set(note)
    }

    private fun addUploadRecordToDb(uri: String, note: Note){
        db.collection("users").document(uid!!).collection("notes").document(note.id!!)
            .set(note)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(activity, "Saved to Firestore", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(activity, "Error saving to Firestore", Toast.LENGTH_LONG).show()
            }
    }

    private fun uploadImage(note: Note){
        var list = imageListToUpload
        for (img in list) {
            imgUri = Uri.parse(img)
            if (imgUri != null) {
                val ref = storageReference?.child("images")?.child(uid!!)?.child(note.id!!)
                    ?.child(getRandomString() + ".jpg")
                val uploadTask = ref?.putFile(imgUri!!)

                // After uploading a file, you can get a URL to download the file by calling the getDownloadUrl() method on the StorageReference:

                val urlTask =
                    uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        return@Continuation ref.downloadUrl
                    })?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val downloadUri = task.result
                            addUploadRecordToDb(downloadUri.toString(), note)
                        } else {
                            // Handle failures
                        }
                    }?.addOnFailureListener {
                    }
            } else {
                Toast.makeText(activity, "Error Uploading Image", Toast.LENGTH_SHORT).show()
            }
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

    private fun askCameraPermission(){
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
                    .setTitle("Permissions Error!")
                    .setMessage("Please allow permissions to take photo with camera")
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
        if (resultCode == RESULT_OK && requestCode == TAKE_PHOTO_REQUEST) {
            image = fileUri.toString()
            CropImage.activity(fileUri)
                .setMinCropWindowSize(500, 500)
                .setAspectRatio(1,1)
                .start(context!!,this)
        }
        if (resultCode == RESULT_OK && requestCode == PICK_PHOTO_REQUEST) {
            fileUri = data?.data
            image = fileUri.toString()
            CropImage.activity(fileUri)
                .setMinCropWindowSize(500, 500)
                .setAspectRatio(1,1)
                .start(context!!,this)
        }
        if (requestCode === CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            var result = CropImage.getActivityResult(data)
            if (resultCode === RESULT_OK) {
                val resultUri = result.uri
                image = resultUri.toString()
                imageListToShow.add(image!!)
                imageListToUpload.add(image!!)
                allFilesList.add(image!!)
                setImages(allFilesList)
            } else if (resultCode === CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
                Toast.makeText(activity, "Error:$result.error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkInputs(noteTitle: String, noteBody: String): Boolean{
        if (noteTitle.isEmpty()) {
            return false
        }

        if (noteBody.isEmpty()) {
            return false
        }
        return true
    }

    private fun onBackPressed() {

        val noteTitle = edit_text_title.text.toString().trim()
        val noteBody = edit_text_note.text.toString().trim()

        if (checkInputs(noteTitle, noteBody)) {
            AlertDialog.Builder(context).apply {
                setTitle("Save your changes or discard them?")
                setPositiveButton("Save") { _, _ ->
                    save()
                }
                setNeutralButton("Cancel") { _, _ ->
                }
                setNegativeButton("Discard") { _, _ ->
                    val action =
                        AddNoteFragmentDirections.actionSaveNote()
                    view?.let { it1 -> Navigation.findNavController(it1).navigate(action) }
                }
            }.create().show()
        } else {
            val action =
                AddNoteFragmentDirections.actionSaveNote()
            view?.let { it1 ->
                Navigation.findNavController(it1).navigate(action)
            }
        }
    }

    private fun deleteNote() {
        AlertDialog.Builder(context).apply {
            setTitle("Are you sure?")
            setMessage("You cannot undo this operation")
            setPositiveButton("Yes") { _, _ ->
                deleteNoteFirestore(note!!)
                launch {
                    NoteDatabase(context).getNoteDao().deleteNote(note!!)
                    val action =
                        AddNoteFragmentDirections.actionSaveNote()
                    Navigation.findNavController(view!!).navigate(action)
                }
            }
            setNegativeButton("No") { _, _ ->
            }
        }.create().show()
    }

    private fun addFile() {
        val mDialogView = LayoutInflater.from(activity).inflate(R.layout.custom_dialog_layout, null)
        val mBuilder = AlertDialog.Builder(activity)
            .setView(mDialogView)
        val  mAlertDialog = mBuilder.show()
        mDialogView.btn_camera.setOnClickListener {
            pickPhotoFromGallery()
            mAlertDialog.dismiss()
        }
        mDialogView.btn_gallery.setOnClickListener {
            askCameraPermission()
            mAlertDialog.dismiss()
        }
        mDialogView.btn_recorder.setOnClickListener {
            askAudioRecoroderPermission()
            mAlertDialog.dismiss()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> save()
            R.id.delete -> if (note != null) deleteNote() else context?.toast("Cannot Delete")
            R.id.add_image_menu_item -> addFile()
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
    }

    private fun initMediaRecorder() {
        mediaRecorder = MediaRecorder()
        val root = Environment.getExternalStorageDirectory().toString()
        val myDir = File("$root/MyApp/saved_audio_records")
        myDir.mkdirs()
        output = myDir.toString()
        startRecording()
    }

    private fun showAudioOpenDialog(){
        val recorderDialogView = LayoutInflater.from(activity).inflate(R.layout.audio_recorder_layout, null)
        val recorderDialogViewBuilder = AlertDialog.Builder(activity)
            .setView(recorderDialogView)
        val  recorderDialog = recorderDialogViewBuilder.show()
        recorderDialog.getWindow().setLayout(300, 300);
        recorderDialogView.btn_stop_recording.setOnClickListener {
            stopRecording()
            recorderDialog.dismiss()
        }
    }

    private fun askAudioRecoroderPermission(){
        Dexter.withActivity(activity).withPermissions(
            Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport) {/* ... */
                if(report.areAllPermissionsGranted()){
                    initMediaRecorder()
                }else{
                }
            }
            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?, token: PermissionToken?
            ) { context?.let {
                AlertDialog.Builder(it)
                    .setTitle("Permissions Error!")
                    .setMessage("Please allow permissions to start recording")
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

    private fun startRecording() {
        output = output + "/" + getRandomString() + ".mp3"
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(output)

        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            state = true
            showAudioOpenDialog()
            Toast.makeText(activity!!, "Recording started!", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalStateException) { e.printStackTrace()
        } catch (e: IOException) { e.printStackTrace() }
    }

    /* @SuppressLint("RestrictedApi", "SetTextI18n")
     @TargetApi(Build.VERSION_CODES.N)
     private fun pauseRecording() {
         if(state) {
             if(!recordingStopped){
                 Toast.makeText(activity!!,"Stopped!", Toast.LENGTH_SHORT).show()
                 mediaRecorder?.pause()
                 recordingStopped = true
                 button_pause_recording.text = "Resume"
             } else{
                 resumeRecording()
             }
         }
     }


     @SuppressLint("RestrictedApi", "SetTextI18n")
     @TargetApi(Build.VERSION_CODES.N)
     private fun resumeRecording() {
         Toast.makeText(activity!!,"Resume!", Toast.LENGTH_SHORT).show()
         mediaRecorder?.resume()
         button_pause_recording.text = "Pause"
         recordingStopped = false
     }*/

    private fun stopRecording(){
        if(state){
            mediaRecorder?.stop()
            mediaRecorder?.release()
            state = false
            saveAudioFile()
        } else{
            Toast.makeText(activity!!, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveAudioFile(){
        audioFilesList.add(output.toString())
        allFilesList.add(output.toString())
        setImages(allFilesList)
    }

    companion object {
        private val ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm"
    }

    private fun getRandomString(): String {
        val random = Random()
        val sb = StringBuilder(MAX_LENGTH)
        for (i in 0 until MAX_LENGTH)
            sb.append(
                ALLOWED_CHARACTERS[random.nextInt(
                    ALLOWED_CHARACTERS.length)])
        return sb.toString()
    }
}
