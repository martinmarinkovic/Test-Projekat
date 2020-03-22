package com.martinmarinkovic.myapplication

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
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
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.martinmarinkovic.myapplication.roomdb.Note
import com.martinmarinkovic.myapplication.roomdb.NoteDatabase
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.audio_recorder_layout.view.*
import kotlinx.android.synthetic.main.custom_dialog_layout.view.*
import kotlinx.android.synthetic.main.fragment_add_note.*
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class AddNoteFragment : BaseFragment() {

    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    private var uid: String? = null
    private var note: Note? = null
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private var ref = db.collection("users")
    private val TAKE_PHOTO_REQUEST: Int = 2
    private val PICK_PHOTO_REQUEST: Int = 1
    private var fileUri: Uri? = null
    private var image: String? = null
    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private var recordingStopped: Boolean = false
    private lateinit var mediaPlayer: MediaPlayer
    private val MAX_LENGTH = 10
    private var imageList: ArrayList<String> = ArrayList()

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_add_note, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        uid = FirebaseAuth.getInstance().currentUser?.uid

        mediaRecorder = MediaRecorder()
        output = Environment.getExternalStorageDirectory().absolutePath + "/recording.mp3"

        // Room db u Kotlinu ne moze da se izvrsava u main thread-u, zato koristimo AsyncTask (doInBackground)
        // Kotlin sadrzi coroutines koje nam pomazu da pisemo ainshorne programe
        // ispred svake funkcije (u NoteDao) pisemo suspend, tako da ona moze da se zove u okviru courutine scope-a

        arguments?.let {
            note = AddNoteFragmentArgs.fromBundle(it).note
            edit_text_title.setText(note?.title)
            edit_text_note.setText(note?.note)
            activity!!.toast("STA presledjuje na pocetku!?!?!?" + note?.images?.toString())
            if(note?.images != null) {
                activity!!.toast("uso li si?????" + note?.images?.toString())
                imageList = note?.images!!
                setImages()
            }
        }

        button_save.setOnClickListener { view ->

            val noteTitle = edit_text_title.text.toString().trim()
            val noteBody = edit_text_note.text.toString().trim()
            activity!!.toast("STA je on ucotaaoooo!?!?!?" + image.toString())
            //image?.let { imageList!!.add(it) }

            if (noteTitle.isEmpty()) {
                edit_text_title.error = "title required"
                edit_text_title.requestFocus()
                return@setOnClickListener
            }

            if (noteBody.isEmpty()) {
                edit_text_note.error = "note required"
                edit_text_note.requestFocus()
                return@setOnClickListener
            }

            launch {

                context?.let {
                    activity!!.toast("Sta on snimaa?!?1!?" + imageList.toString())
                    val mNote = Note(noteTitle, noteBody, imageList)

                    if (note == null) {
                        NoteDatabase(it).getNoteDao().addNote(mNote)
                        it.toast("Note Saved")
                    } else {
                        mNote.id = note!!.id
                        NoteDatabase(it).getNoteDao().updateNote(mNote)
                        it.toast("Note Updated")
                    }


                    val action = AddNoteFragmentDirections.actionSaveNote()
                    Navigation.findNavController(view).navigate(action)
                }
            }

        }

        btn_start_audio_file.setOnClickListener{

            val action = AddNoteFragmentDirections.actionOpenNoteImages()
            Navigation.findNavController(it).navigate(action)

            /*mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(output)
            mediaPlayer.prepare()
            mediaPlayer.start()*/
        }
    }

    private fun setImages(){
        val sglm = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        rv.layoutManager = sglm
        val igka = NoteImageAdapter(activity!!, imageList)
        rv.adapter = igka
    }

    private fun saveNote(note: Note) {
        if (uid != null) {
            var ref = db.collection("users").document(uid!!)
            ref.get().addOnSuccessListener { documentSnapshot ->
                //val user = documentSnapshot.toObject<User>()

                db.collection("users").document(uid!!).collection("notes").document(note.id.toString())
                    .set(note)
                    .addOnSuccessListener { documentReference ->
                        Toast.makeText(activity, "Success!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(activity, "Fail!", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun getListOfNotes() : List<Note> {
        val notes = ArrayList<Note>()
        uid?.let {
            ref.document(it)
                .collection("notes")
                .get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            var note = document.toObject<Note>()
                            //note.id = id.toInt()
                            notes.add(note)
                        }
                        Toast.makeText(activity, notes.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
        }
        return notes;
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
                imageList.add(image!!)
                Toast.makeText(activity, "Lista!!" + imageList, Toast.LENGTH_SHORT).show()
                setImages()
            } else if (resultCode === CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Toast.makeText(activity, "Error:" + error.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addUploadRecordToDb(uri: String){
        val data = HashMap<String, Any>()
        data["image"] = uri

        db.collection("users").document(uid!!).collection("notes").document(note!!.id.toString())
            .update(data)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(activity, "Saved to Firestore", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(activity, "Error saving to Firestore", Toast.LENGTH_LONG).show()
            }
    }

    private fun uploadImage(){
        if(fileUri != null){
            val ref = storageReference?.child("images")?.child(uid!!)?.child(note!!.id.toString())?.child(getRandomString() + ".jpg")
            val uploadTask = ref?.putFile(fileUri!!)

            //UUID.randomUUID().toString() - random id
            // After uploading a file, you can get a URL to download the file by calling the getDownloadUrl() method on the StorageReference:
            // Continuation - function that is called to continue execution after completion of a Task.

            val urlTask = uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            })?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    addUploadRecordToDb(downloadUri.toString())
                } else {
                    // Handle failures
                }
            }?.addOnFailureListener{

            }
        }else{
            Toast.makeText(activity, "Please Upload an Image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onBackPressed() {
        AlertDialog.Builder(context).apply {
            setTitle("Are you sure?")
            setMessage("You cannot undo this operation")
            setPositiveButton("UploadImage") { _, _ ->
                uploadImage()
            }
            setNeutralButton("GetListOfAllNotes") { _, _ ->
                getListOfNotes()
            }
            setNegativeButton("SaveNoteToFirebase") { _, _ ->
                note?.let { saveNote(it) }
            }
        }.create().show()
    }

    private fun deleteNote() {
        AlertDialog.Builder(context).apply {
            setTitle("Are you sure?")
            setMessage("You cannot undo this operation")
            setPositiveButton("Yes") { _, _ ->
                launch {
                    NoteDatabase(context).getNoteDao().deleteNote(note!!)
                    val action = AddNoteFragmentDirections.actionSaveNote()
                    Navigation.findNavController(view!!).navigate(action)
                }
            }
            setNegativeButton("No") { _, _ ->
            }
        }.create().show()
    }

    private fun addImage() {
        val mDialogView = LayoutInflater.from(activity).inflate(R.layout.custom_dialog_layout, null)
        val mBuilder = AlertDialog.Builder(activity)
            .setView(mDialogView)
            .setTitle("Select:")
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
            startAudioRecoroder()
            mAlertDialog.dismiss()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> if (note != null) deleteNote() else context?.toast("Cannot Delete")
            R.id.add_image_menu_item -> addImage()
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
    }

    private fun startAudioRecoroder(){
        if (ContextCompat.checkSelfPermission(activity!!,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(activity!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(activity!!, permissions,0)
        } else {
            startRecording()
        }
        val recorderDialogView = LayoutInflater.from(activity).inflate(R.layout.audio_recorder_layout, null)
        val recorderDialogViewBuilder = AlertDialog.Builder(activity)
            .setView(recorderDialogView)
        val  recorderDialog = recorderDialogViewBuilder.show()
        recorderDialogView.btn_stop_recording.setOnClickListener {
            stopRecording()
            recorderDialog.dismiss()
        }
    }

    private fun startRecording() {
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(output)

        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            state = true
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
            saveFilePath()
        } else{
            Toast.makeText(activity!!, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveFilePath(){
    }

    companion object {
        private val ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm"
    }

    private fun getRandomString(): String {
        val random = Random()
        val sb = StringBuilder(MAX_LENGTH)
        for (i in 0 until MAX_LENGTH)
            sb.append(ALLOWED_CHARACTERS[random.nextInt(ALLOWED_CHARACTERS.length)])
        return sb.toString()
    }
}