package com.martinmarinkovic.myapplication.usersettings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.EmailAuthProvider.getCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.martinmarinkovic.myapplication.R
import com.martinmarinkovic.myapplication.roomdb.User
import com.martinmarinkovic.myapplication.helper.toast
import com.martinmarinkovic.myapplication.login.SignInActivity
import com.martinmarinkovic.myapplication.notes.BaseFragment
import com.martinmarinkovic.myapplication.roomdb.Note
import com.martinmarinkovic.myapplication.roomdb.NoteDatabase
import kotlinx.android.synthetic.main.fragment_user_settings.*
import kotlinx.android.synthetic.main.fragment_user_settings.tv_username
import kotlinx.coroutines.launch

private var firebaseStore: FirebaseStorage? = null
private var storageReference: StorageReference? = null
private var uid: String? = null
private var note: Note? = null
private lateinit var auth: FirebaseAuth
private val db = Firebase.firestore
private var ref = db.collection("users")
private var user: FirebaseUser? = null
private var empty = ArrayList<String>()

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

        setUsername()

        btn_logout.setOnClickListener {
            signOut()
        }

        btn_restore.setOnClickListener {
            restoreNotes()
        }

        btn_delete.setOnClickListener{
            deleteUserFromFirestore()
        }
    }

    private fun deleteImagesFromStorage(noteId: String) {
        val ref = storageReference?.child("images")?.child(
            uid!!)?.child(noteId)
        ref?.listAll()?.addOnSuccessListener { result ->
            for (fileRef in result.items)
                fileRef.delete()
        }?.addOnFailureListener {
            activity?.toast("Error: Delete Images From Storage!")
        }
    }

    private fun deleteUserFromFirestore() {
        if (uid != null) {
            var ref = db.collection("users").document(
                uid!!)
            ref.delete()
                .addOnSuccessListener {
                    ref.get().addOnSuccessListener {
                        ref.collection("notes").get().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                deleteAuthUser()
                                for (document in task.result!!) {
                                    var note = document.toObject<Note>()
                                    document.reference.delete()
                                    launch {
                                        NoteDatabase(context!!).getNoteDao().deleteNote(note)
                                        deleteImagesFromStorage(note.id!!)
                                    }
                                }
                            }
                        }
                    }
                }.addOnFailureListener() {
                    activity?.toast("Error: Delete User From Firestore!")
                }
        }
    }

    private fun deleteAuthUser() {
        val user = FirebaseAuth.getInstance().currentUser
        val credential = getCredential("user@example.com", "password1234")
        user?.reauthenticate(credential)?.addOnCompleteListener {
            signOut()
            user.delete()
        }
    }

    private fun restoreNotes() {
        ref.document(
            uid!!).collection("notes").get().addOnCompleteListener { task ->
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

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(activity, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun setUsername() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            var ref = db.collection("users").document(firebaseUser.uid)
            ref.get().addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject<User>()
                if (user != null) {
                    tv_username.text = user.username
                }
            }
        }
    }
}