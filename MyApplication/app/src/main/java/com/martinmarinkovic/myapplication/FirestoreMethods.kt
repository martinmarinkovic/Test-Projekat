package com.martinmarinkovic.myapplication

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FirestoreMethods {

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
}