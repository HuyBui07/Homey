package com.example.homey.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.homey.R
import com.example.homey.data.model.Estate
import com.example.homey.data.repository.EstateRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.homey.adapters.EstateAdapter
import com.google.firebase.auth.FirebaseAuth

class MyEstatesActivity : AppCompatActivity() {
    private val estateRepo = EstateRepository.getInstance()
    private lateinit var estateAdapter: EstateAdapter
    private val uid = FirebaseAuth.getInstance().currentUser?.uid

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                uid?.let { userId ->
                    estateRepo.getEstatesByOwner(userId) { estates ->
                        if (estates != null) {
                            updateEstates(estates)
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_estates)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set the title of the action bar
        supportActionBar?.title = "My Real Estates"

        // Enable the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set up add estate button
        val addEstateButton = findViewById<FloatingActionButton>(R.id.fab)
        addEstateButton.setOnClickListener {
            val intent = Intent(this, AddRealEstateActivity::class.java)
            startForResult.launch(intent)
        }

        // Fetch and display estates
        uid?.let { userId ->
            estateRepo.getEstatesByOwner(userId) { estates ->
                if (estates != null) {
                    displayEstates(estates)
                } else {
                    showNoEstatesMessage()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun displayEstates(estates: List<Estate>) {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val noEstatesTextView = findViewById<TextView>(R.id.noEstatesTextView)

        if (estates.isEmpty()) {
            recyclerView.visibility = RecyclerView.GONE
            noEstatesTextView.visibility = TextView.VISIBLE
        } else {
            noEstatesTextView.visibility = TextView.GONE
            recyclerView.visibility = RecyclerView.VISIBLE
            recyclerView.layoutManager = LinearLayoutManager(this)
            estateAdapter = EstateAdapter(estates, startForResult)
            recyclerView.adapter = estateAdapter
        }
    }

    private fun updateEstates(estates: List<Estate>) {
        if (::estateAdapter.isInitialized) {
            estateAdapter.updateEstates(estates)
        } else {
            displayEstates(estates)
        }
    }

    private fun showNoEstatesMessage() {
        val noEstatesTextView = findViewById<TextView>(R.id.noEstatesTextView)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        noEstatesTextView.visibility = TextView.VISIBLE
        recyclerView.visibility = RecyclerView.GONE
    }
}
