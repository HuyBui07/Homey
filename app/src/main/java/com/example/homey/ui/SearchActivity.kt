package com.example.homey.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.homey.R
import com.example.homey.adapters.EstateAdapter
import com.example.homey.data.model.Estate
import com.example.homey.data.repository.EstateRepository

class SearchActivity : AppCompatActivity() {
    private lateinit var searchInput: EditText
    private lateinit var searchResults: RecyclerView
    private lateinit var noResultsText: TextView
    private val estateRepository = EstateRepository.getInstance()
    private lateinit var adapter: EstateAdapter
    private lateinit var startForResult: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchInput = findViewById(R.id.searchInput)
        searchResults = findViewById(R.id.searchResults)
        noResultsText = findViewById(R.id.noResultsText)

        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val updatedEstateId = data?.getStringExtra("updatedEstateId")
                val query = searchInput.text.toString().trim()
                if (query.isNotEmpty()) {
                    searchEstates(query)
                }
            }
        }

        adapter = EstateAdapter(emptyList(), startForResult)
        searchResults.layoutManager = LinearLayoutManager(this)
        searchResults.adapter = adapter

        searchInput.addTextChangedListener { text ->
            val query = text.toString().trim()
            if (query.isNotEmpty()) {
                searchEstates(query)
            } else {
                noResultsText.visibility = View.GONE
                searchResults.visibility = View.GONE
                adapter.updateEstates(emptyList())
            }
        }
    }

    private fun searchEstates(query: String) {
        estateRepository.searchEstatesByName(query) { estates ->
            if (estates == null) {
                noResultsText.text = getString(R.string.error_message)
                noResultsText.visibility = View.VISIBLE
                searchResults.visibility = View.GONE
            } else if (estates.isEmpty()) {
                noResultsText.text = getString(R.string.no_results_message)
                noResultsText.visibility = View.VISIBLE
                searchResults.visibility = View.GONE
            } else {
                noResultsText.visibility = View.GONE
                searchResults.visibility = View.VISIBLE
                adapter.updateEstates(estates)
            }
        }
    }
}
