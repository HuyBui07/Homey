package com.example.homey.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.homey.R
import com.example.homey.adapters.PostAdapter
import com.example.homey.data.model.Post
import com.example.homey.data.repository.UserRepository
import com.example.homey.data.repository.EstateRepository

class FavoriteFragment : Fragment(), PostAdapter.PostAdapterCallback {
    private val userRepository = UserRepository.getInstance()
    private val estateRepository = EstateRepository.getInstance()
    private lateinit var postAdapter: PostAdapter
    private lateinit var posts: MutableList<Post>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Hide action bar
        (activity as? AppCompatActivity)?.supportActionBar?.hide()

        val favoriteEstateList = userRepository.getFavorites()
        Log.d("FavoriteFragment", "Favorite estates: $favoriteEstateList")

        posts = mutableListOf<Post>()
        postAdapter = PostAdapter(requireContext(), posts, this)
        view.findViewById<ListView>(R.id.itemPost).adapter = postAdapter

        if (!favoriteEstateList.isNullOrEmpty()) {
            for (estateId in favoriteEstateList) {
                estateRepository.getEstateById(estateId) { estate ->
                    userRepository.getAvatarAndUsernameAndPhoneNumberAndFavoriteState(estate!!.ownerUid, estate.id!!) { success, avatar, username, phoneNumber, isFavorite ->
                        if (success && username != null && phoneNumber != null  && isFavorite != null && avatar != null) {
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
                        if (posts.size == favoriteEstateList.size) {
                            postAdapter.notifyDataSetChanged()
                        }
                    }
                }

            }
        } else {
            val noEstatesTextView = view.findViewById<TextView>(R.id.noEstatesTextView)
            noEstatesTextView.visibility = View.VISIBLE
        }


        return view
    }

    override fun onFavoriteButtonClicked(postId: String) {
        posts.remove(posts.find { it.id == postId })
        if (posts.isEmpty()) {
            view?.findViewById<TextView>(R.id.noEstatesTextView)?.visibility = View.VISIBLE
        }
        postAdapter.notifyDataSetChanged()
        val fragmentManager = parentFragmentManager
        val homeFragment = fragmentManager.findFragmentByTag("HOME_FRAGMENT")

        if (homeFragment != null) {
            fragmentManager.beginTransaction().apply {
                detach(homeFragment)
                commit()
            }
            fragmentManager.beginTransaction().apply {
                attach(homeFragment)
                commit()
            }
        }
    }
}