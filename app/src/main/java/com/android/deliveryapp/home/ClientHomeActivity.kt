package com.android.deliveryapp.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.android.deliveryapp.R
import com.android.deliveryapp.databinding.ActivityClientHomeBinding
import com.android.deliveryapp.util.ProductItem
import com.google.firebase.firestore.FirebaseFirestore

class ClientHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientHomeBinding
    private lateinit var database: FirebaseFirestore
    private lateinit var productList: Array<ProductItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseFirestore.getInstance()

        // TODO: 10/02/2021 get collection 
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.client_home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clientProfile -> {
                // TODO: 07/02/2021 return to client profile
                true
            }
            R.id.orders -> {
                // TODO: 07/02/2021 start activity orders
                true
            }
            R.id.shoppingCart -> {
                // TODO: 07/02/2021 start activity shopping cart
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}