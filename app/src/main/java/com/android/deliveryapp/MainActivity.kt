package com.android.deliveryapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.client.ClientHomeActivity
import com.android.deliveryapp.manager.ManagerHomeActivity
import com.android.deliveryapp.rider.RiderHomeActivity
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
import com.google.firebase.auth.FirebaseAuth

/**
 * Splash screen activity
 */
class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide() // hide action bar

        auth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            val sharedPreferences = getSharedPreferences(userInfo, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            if (sharedPreferences.getBoolean(isRegistered, false)) {
                if (sharedPreferences.getBoolean(isLogged, false)) {
                    val email = sharedPreferences.getString(username, null)
                    val password = sharedPreferences.getString(pwd, null)

                    // if client is already registered, so has already a location
                    editor.putBoolean(hasLocation, true)
                    editor.apply()

                    auth.signInWithEmailAndPassword(email ?: "error", password ?: "error")
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                Log.d("FIREBASE_AUTH", "User logged successfully")
                                when (sharedPreferences.getString(userType, null)) {
                                    CLIENT -> startActivity(
                                        Intent(
                                            this@MainActivity,
                                            ClientHomeActivity::class.java
                                        )
                                    )
                                    RIDER -> startActivity(
                                        Intent(
                                            this@MainActivity,
                                            RiderHomeActivity::class.java
                                        )
                                    )
                                    MANAGER -> startActivity(
                                        Intent(
                                            this@MainActivity,
                                            ManagerHomeActivity::class.java
                                        )
                                    )
                                    else -> startActivity(
                                        Intent(
                                            this@MainActivity,
                                            SelectUserTypeActivity::class.java
                                        )
                                    )
                                }
                            } else {
                                Log.w("FIREBASE_AUTH", "Failed to log user", task.exception)
                                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                            }
                        }
                } else {
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                } // TODO: 24/02/2021 put invalid user 
            } else {
                editor.putBoolean(hasLocation, false)
                editor.apply()
                startActivity(Intent(this@MainActivity, SelectUserTypeActivity::class.java))
            }
        }, 1500) // wait 1.5 seconds, then show the activity

    }
}