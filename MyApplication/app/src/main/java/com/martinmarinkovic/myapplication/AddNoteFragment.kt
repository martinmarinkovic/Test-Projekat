package com.martinmarinkovic.myapplication

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.martinmarinkovic.myapplication.roomdb.Note
import com.martinmarinkovic.myapplication.roomdb.NoteDatabase
import kotlinx.android.synthetic.main.fragment_add_note.*
import kotlinx.coroutines.launch


class AddNoteFragment : BaseFragment() {

    private var note: Note? = null
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    var ref = db.collection("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_add_note, container, false)
    }

    // Room db u Kotlinu ne moze da se izvrsava u main thread-u, zato koristimo AsyncTask (doInBackground)
    // Kotlin sadrzi coroutines koje nam pomazu da pisemo ainshorne programe
    // ispred svake funkcije (u NoteDao) pisemo suspend, tako da ona moze da se zove u okviru courutine scope-a

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Primam argumant koj je prosledjen iz HomeFragmenta
        arguments?.let {
            note = AddNoteFragmentArgs.fromBundle(it).note
            edit_text_title.setText(note?.title)
            edit_text_note.setText(note?.note)
            var image = AddNoteFragmentArgs.fromBundle(it).image
            if (image != null) {
                var fileUri = Uri.parse(image)

                Glide.with(activity!!)
                    .load(fileUri)
                    .into(image_view);
            }
        }

        button_save.setOnClickListener { view ->

            val noteTitle = edit_text_title.text.toString().trim()
            val noteBody = edit_text_note.text.toString().trim()

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

            // launch Coroutine scope
            launch {

                context?.let {
                    val mNote = Note(noteTitle, noteBody)

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
    }

    private fun onBackPressed() {
        AlertDialog.Builder(context).apply {
            setTitle("Are you sure?")
            setMessage("You cannot undo this operation")
            setPositiveButton("Cancel") { _, _ ->

            }
            setNeutralButton("Discard") { _, _ ->

                getListOfNotes()
            }
            setNegativeButton("Save") { _, _ ->

                saveNote()
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

    private fun showPopUp() {
        //val action = AddNoteFragmentDirections.actionOpenPopUp()
        Navigation.findNavController(view!!).navigate(R.id.popUpFragment)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> if (note != null) deleteNote() else context?.toast("Cannot Delete")
            R.id.popup_menu_item -> showPopUp()
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
    }

    private fun saveNote() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            var ref = db.collection("users").document(uid)
            ref.get().addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject<User>()

                var note = Note("test", "proba")
                db.collection("users").document(uid).collection("notes").document()   //note id?????
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

    /*private fun updateNote() {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        ref.document(uid)
            .collection("notes")
            .document("123")
            .update()
    }*/

    private fun getListOfNotes() : List<Note> {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val notes = ArrayList<Note>()
        ref.document(uid)
            .collection("notes")
            .get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result!!) {
                    val title = document.data["title"].toString()
                    val content = document.data["note"].toString()
                    val note = Note(title, content)
                    notes.add(note)
                }
                Toast.makeText(activity, notes.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        return notes;
    }
}
