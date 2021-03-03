package com.android.deliveryapp.manager

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.databinding.ActivityManagerChatBinding
import com.android.deliveryapp.rider.RiderChatActivity
import com.android.deliveryapp.util.Keys.Companion.MANAGER
import com.android.deliveryapp.util.Keys.Companion.chatCollection
import com.android.deliveryapp.util.Keys.Companion.managerPref
import com.android.deliveryapp.util.Keys.Companion.riderEmail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class ManagerChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManagerChatBinding
    private lateinit var auth: FirebaseAuth

    companion object {
        const val NAME = "NAME"
        const val TEXT = "TEXT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagerChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // show a back arrow button in actionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser

        if (user != null) {

            val sharedPreferences = getSharedPreferences(managerPref, Context.MODE_PRIVATE)

            val riderEmail = sharedPreferences.getString(riderEmail, "")

            val firestoreChat by lazy {
                FirebaseFirestore.getInstance().collection(chatCollection)
                        .document("$riderEmail|$MANAGER")
            }

            updateChat(firestoreChat)

            binding.sendMsgBtn.setOnClickListener {
                sendMessage(firestoreChat)
                binding.message.text?.clear()
            }

        }
    }

    private fun sendMessage(reference: DocumentReference) {
        val newMessage = mapOf(
                RiderChatActivity.NAME to "Rider",
                RiderChatActivity.TEXT to binding.message.text.toString()
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
                        binding.messageTextView.append("${data?.get(RiderChatActivity.NAME)}:${data?.get(RiderChatActivity.TEXT)}\n")
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