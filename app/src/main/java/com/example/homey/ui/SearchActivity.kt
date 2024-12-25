package com.example.homey.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.homey.R
import com.example.homey.adapters.PostAdapter
import com.example.homey.data.model.Post
import com.example.homey.data.repository.EstateRepository
import com.example.homey.data.repository.UserRepository
import com.example.homey.utils.StringUtils.removeVietnameseAccents
import java.util.Locale

class SearchActivity : AppCompatActivity(), PostAdapter.PostAdapterCallback {
    private lateinit var searchInput: EditText
    private lateinit var noResultsText: TextView
    private lateinit var listView: ListView
    private lateinit var adapter: PostAdapter
    private lateinit var posts: MutableList<Post>
    private val estateRepository = EstateRepository.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        setupUI()

        searchInput.setOnEditorActionListener { _, _, _ ->
            val query = normalizeInput(searchInput.text.toString().trim())
            if (query.isNotEmpty()) {
                searchEstatesByLocation(query)
            }
            false
        }
    }

    private fun setupUI() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.apply {
            title = "Search Estates"
            setDisplayHomeAsUpEnabled(true)
        }

        searchInput = findViewById(R.id.searchInput)
        noResultsText = findViewById(R.id.noResultsText)
        listView = findViewById(R.id.itemPost)

        posts = mutableListOf()
        adapter = PostAdapter(this, posts, this)
        listView.adapter = adapter
    }

    private fun normalizeInput(input: String): String {
        return removeVietnameseAccents(input.trim().lowercase(Locale.getDefault()))
    }

    private fun searchEstatesByLocation(location: String) {
        estateRepository.searchEstatesByLocation(location) { estates ->
            if (estates.isNotEmpty()) {
                posts.clear()
                for (estate in estates) {
                    if (posts.none { it.id == estate.id }) {
                        UserRepository.getInstance().getAvatarAndUsernameAndPhoneNumberAndFavoriteState(
                            estate.ownerUid, estate.id!!
                        ) { success, avatar, username, phoneNumber, isFavorite ->
                            if (success && avatar != null && username != null && phoneNumber != null && isFavorite != null) {
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
                                    isFavorite
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
            } else {
                noResultsText.visibility = View.VISIBLE
                listView.visibility = View.GONE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onFavoriteButtonClicked(postId: String) {
        Log.d("SearchActivity", "Favorite button clicked for post: $postId")
    }
}
