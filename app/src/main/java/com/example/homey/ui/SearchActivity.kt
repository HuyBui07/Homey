package com.example.homey.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.homey.R
import com.example.homey.adapters.EstateAdapter
import com.example.homey.adapters.PostAdapter
import com.example.homey.data.model.Estate
import com.example.homey.data.model.Post
import com.example.homey.data.repository.EstateRepository
import com.example.homey.data.repository.UserRepository

class SearchActivity : AppCompatActivity(), PostAdapter.PostAdapterCallback {
    private lateinit var searchInput: EditText
    private lateinit var noResultsText: TextView
    private val estateRepository = EstateRepository.getInstance()
    private lateinit var adapter: PostAdapter
    private lateinit var posts: MutableList<Post>
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Set the title of the action bar
        supportActionBar?.title = "Searching for estates"

        // Enable the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        noResultsText = findViewById(R.id.noResultsText)

        posts = mutableListOf<Post>()
        adapter = PostAdapter(this, posts, this)
        listView = findViewById(R.id.itemPost)
        listView.adapter = adapter

        searchInput = findViewById(R.id.searchInput)

        searchInput.addTextChangedListener { text ->
            val query = text.toString().trim()
            if (query.isNotEmpty()) {
                searchEstates(query)
            } else {
                noResultsText.visibility = View.GONE
                listView.visibility = View.GONE
                adapter.updatePosts(emptyList())
            }
        }
    }

    private fun searchEstates(query: String) {
        estateRepository.searchEstatesByName(query) { estates ->
            if (estates.isNotEmpty()) {
                for (estate in estates) {
                    if (posts.none { it.id == estate.id }) {
                        UserRepository.getInstance().getAvatarAndUsernameAndPhoneNumberAndFavoriteState(estate.ownerUid, estate.id!!) { success, avatar, username, phoneNumber, isFavorite ->
                            if (success && username != null && phoneNumber != null && isFavorite != null && avatar != null) {
                                val post = Post(
                                    estate.id!!,
                                    estate.images[0],
                                    estate.images[1],
                                    estate.images[2],
                                    estate.images[3],
                                    estate.title,
                                    estate.propertyType,
                                    estate.price,
                                    estate.size,
                                    estate.location,
                                    avatar,
                                    username,
                                    phoneNumber,
                                    estate.bedrooms,
                                    estate.bathrooms,
                                    estate.postTime,
                                    estate.description,
                                    estate.frontage,
                                    estate.orientation,
                                    estate.legalStatus,
                                    estate.furnishings,
                                    isFavorite,
                                )
                                posts.add(post)
                            }
                            if (posts.size == estates.size) {
                                adapter.updatePosts(posts)
                                listView.visibility = View.VISIBLE
                                noResultsText.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onFavoriteButtonClicked(postId: String) {
        setResult(Activity.RESULT_OK)
    }
}
