package com.android.deliveryapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.client.ClientHomeActivity
import com.android.deliveryapp.databinding.ActivityLoginBinding
import com.android.deliveryapp.manager.ManagerHomeActivity
import com.android.deliveryapp.rider.RiderProfileActivity
import com.android.deliveryapp.util.Keys.Companion.CLIENT
import com.android.deliveryapp.util.Keys.Companion.MANAGER
import com.android.deliveryapp.util.Keys.Companion.RIDER
import com.android.deliveryapp.util.Keys.Companion.hasLocation
import com.android.deliveryapp.util.Keys.Companion.isLogged
import com.android.deliveryapp.util.Keys.Companion.isRegistered
import com.android.deliveryapp.util.Keys.Companion.pwd
import com.android.deliveryapp.util.Keys.Companion.userInfo
import com.android.deliveryapp.util.Keys.Companion.userType
import com.android.deliveryapp.util.Keys.Companion.username
import com.android.deliveryapp.util.Keys.Companion.users
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var firestore: FirebaseFirestore

    private val TAG = "EmailPassword"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        sharedPreferences = getSharedPreferences(userInfo, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        binding.submitButton.setOnClickListener {
            loginUser(binding.loginEmail, binding.loginPassword)
        }

        binding.signUpLabel.setOnClickListener {
            editor.clear() // delete all preferences
            editor.apply()

            startActivity(Intent(this@LoginActivity, SelectUserTypeActivity::class.java))
        }
    }

    private fun loginUser(email: TextInputEditText, password: TextInputEditText) {
        if (email.text.isNullOrEmpty()) {
            email.error = getString(R.string.empty_email)
            email.requestFocus()
            return
        }
        if (password.text.isNullOrEmpty()) {
            password.error = getString(R.string.empty_password)
            password.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()) {
            email.error = getString(R.string.invalid_email)
            email.requestFocus()
            return
        }

        auth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val editor = sharedPreferences.edit()

                    if (binding.rememberUser.isChecked) { // if user wants to be remembered next time
                        editor.putBoolean(isLogged, true) // user must be logged instantly next time
                        editor.putString(
                            username,
                            binding.loginEmail.text.toString()
                        ) // save orderEmail
                        editor.putString(
                            pwd,
                            binding.loginPassword.text.toString()
                        ) // save password
                    } else {
                        editor.putBoolean(isLogged, false)
                    }

                    getUserType(firestore, editor, email.text.toString())
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        getString(R.string.login_failure),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun getUserType(firestore: FirebaseFirestore, editor: SharedPreferences.Editor, email: String) {
        var user = String()

        firestore.collection(users)
            .get()
            .addOnSuccessListener { result ->
                for (document in result.documents) {
                    if (document.id == email) {
                        user = document.getString(userType) as String

                        editor.putBoolean(isRegistered, true)
                        editor.apply()

                        when (user) {
                            CLIENT -> {
                                editor.putBoolean(hasLocation, true)
                                startActivity(
                                    Intent(
                                        this@LoginActivity,
                                        ClientHomeActivity::class.java
                                    )
                                )
                                finishAffinity()
                            }
                            RIDER -> {
                                startActivity(
                                    Intent(
                                        this@LoginActivity,
                                        RiderProfileActivity::class.java
                                    )
                                )
                                finishAffinity()
                            }
                            MANAGER -> {
                                startActivity(
                                    Intent(
                                        this@LoginActivity,
                                        ManagerHomeActivity::class.java
                                    )
                                )
                                finishAffinity()
                            }
                            else -> startActivity(
                                Intent(
                                    this@LoginActivity,
                                    SelectUserTypeActivity::class.java
                                )
                            )
                        }
                    }
                }
                editor.putString(userType, user)
                editor.apply()
            }
            .addOnFailureListener { e ->
                Log.w("FIRESTORE", "Failed to get data", e)
                Toast.makeText(
                    baseContext,
                    getString(R.string.error_user_data),
                    Toast.LENGTH_LONG
                ).show()
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