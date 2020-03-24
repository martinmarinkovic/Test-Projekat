package com.martinmarinkovic.myapplication

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.EmailAuthProvider.getCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.martinmarinkovic.myapplication.roomdb.Note
import com.martinmarinkovic.myapplication.roomdb.NoteDatabase
import kotlinx.android.synthetic.main.fragment_user_settings.*
import kotlinx.coroutines.launch

private var firebaseStore: FirebaseStorage? = null
private var storageReference: StorageReference? = null
private var uid: String? = null
private var note: Note? = null
private lateinit var auth: FirebaseAuth
private val db = Firebase.firestore
private var ref = db.collection("users")
private var notes = ArrayList<Note>()
private var user: FirebaseUser? = null

class UserSettingsFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_settings, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        uid = FirebaseAuth.getInstance().currentUser?.uid
        auth = FirebaseAuth.getInstance()
        user = FirebaseAuth.getInstance().currentUser

        btn_logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        btn_restore.setOnClickListener {

            restoreNotes()

            /*launch {
                context?.let {
                    notes = getListOfNotes()
                    for (note in notes)
                        NoteDatabase(it).getNoteDao().addNote(note)
                }
            }*/
        }

        btn_delete.setOnClickListener{

            delete()

           /* val user = FirebaseAuth.getInstance().currentUser

            val credential = EmailAuthProvider.getCredential("user@example.com", "password1234")

            user?.reauthenticate(credential)
                ?.addOnCompleteListener {
                    user.delete()
                }
*/
            //Document + subcollection + userAuth + storage

            /*if (uid != null) {
                var ref = db.collection("users").document(uid!!)
                ref.delete()
                    .addOnSuccessListener {
                    ref.get().addOnSuccessListener { documentSnapshot ->
                        activity?.toast("Upao")
                        ref.collection("notes")
                            .get().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    activity?.toast("Upao i u kolkciju")
                                    for (document in task.result!!) {
                                        document.reference.delete()
                                    }
                                }
                            }
                    }
                }.addOnSuccessListener {
                    activity?.toast("Error!")
                }
            }*/

            //val user = FirebaseAuth.getInstance().currentUser

            /*user?.delete()
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        activity?.toast("Deleted!")
                    } else {
                        activity?.toast("Error!")
                    }
                }*/
        }
    }

    private fun delete() {
        val user = FirebaseAuth.getInstance().currentUser
        val credential = EmailAuthProvider.getCredential("martin.marinkovic3@gmail.com", "mmmmmm")
        activity?.toast("Credential!" + credential.toString())
        user?.reauthenticate(credential)?.addOnCompleteListener {
            user.delete().addOnCompleteListener {
                    if (it.isSuccessful) {
                        activity?.toast("Successfully deleted!")
                        Log.d(TAG, "User account deleted.");
                    } else(
                            activity?.toast("Error!")
                            )
                }
        }
    }

    private fun restoreNotes() {
        ref.document(uid!!).collection("notes").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result!!) {
                    var note = document.toObject<Note>()
                    launch {
                        NoteDatabase(context!!).getNoteDao().addNote(note)
                    }
                }
                activity?.toast("Successfully restored!")
            } else
                activity?.toast("Error!")
        }
    }

   /* private fun getListOfNotes() : ArrayList<Note> {
        var notes = ArrayList<Note>()
        uid?.let {
            ref.document(it)
                .collection("notes")
                .get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            var note = document.toObject<Note>()
                            notes.add(note)
                        }
                    }
                }
        }
        return notes
    }
*/
    private fun deleteFromStorage(url: String){
        val storageReference: StorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(url)
        storageReference.delete()
            .addOnSuccessListener {
            }.addOnFailureListener {
        }
    }
}