package com.android.deliveryapp.client

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.R
import com.android.deliveryapp.databinding.ActivityClientChatBinding
import com.android.deliveryapp.util.Keys.Companion.chatCollection
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class ClientChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientChatBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var reference: DocumentReference

    companion object {
        const val NAME = "NAME"
        const val TEXT = "TEXT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        // show a back arrow button in actionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val user = auth.currentUser

        if (user != null) {
            firestore.collection(chatCollection).get()
                .addOnSuccessListener { result ->
                    // find the chat which contains client email
                    for (document in result.documents) {
                        if (document.id.contains(user.email!!)) {
                            reference = document.reference
                            updateChat(reference)
                            return@addOnSuccessListener
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("FIREBASE_FIRESTORE", "Error getting data", e)
                    Toast.makeText(
                        baseContext,
                        getString(R.string.no_chat_found),
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }

            binding.sendMsgBtn.setOnClickListener {
                sendMessage(reference)
                binding.message.text?.clear()
            }
        }
    }

    private fun sendMessage(reference: DocumentReference) {
        val newMessage = mapOf(
            NAME to "Client",
            TEXT to binding.message.text.toString()
        )

        reference.set(newMessage)
            .addOnSuccessListener {
                Log.d("FIRESTORE_CHAT", "Message sent")
            }
            .addOnFailureListener { e ->
                Log.e("ERROR", e.message.toString())
            }
    }

    private fun updateChat(reference: DocumentReference) {
        reference.addSnapshotListener { value, error ->
            when {
                error != null -> Log.e("ERROR", error.message.toString())
                value != null && value.exists() -> {
                    with(value) {
                        binding.messageTextView.append("${data?.get(NAME)}:${data?.get(
                            TEXT
                        )}\n")
                    }
                }
            }
        }
    }

    // when the back button is pressed in actionbar, finish this activity
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // hide keyboard when user clicks outside EditText
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
        return super.dispatchTouchEvent(event)
    }
}