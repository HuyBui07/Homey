package com.example.homey.adapters

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.homey.R
import com.example.homey.data.model.Post
import com.example.homey.data.repository.UserRepository
import com.example.homey.utils.formatPrice

class PostAdapter(
    private val context: Context,
    private var posts: List<Post>,
    private val callback: PostAdapterCallback
) : BaseAdapter() {

    interface PostAdapterCallback {
        fun onFavoriteButtonClicked(postId: String)
    }

    override fun getCount(): Int = posts.size

    override fun getItem(position: Int): Any = posts[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_mainpage, parent, false)
        val post = posts[position]

        // Ánh xạ các thành phần từ itemPost.xml
        val mainImage = view.findViewById<ImageView>(R.id.mainImage)
        val smallImage1 = view.findViewById<ImageView>(R.id.smallImage1)
        val smallImage2 = view.findViewById<ImageView>(R.id.smallImage2)
        val smallImage3 = view.findViewById<ImageView>(R.id.smallImage3)
        val title = view.findViewById<TextView>(R.id.title)
        val price = view.findViewById<TextView>(R.id.price)
        val area = view.findViewById<TextView>(R.id.area)
        val address = view.findViewById<TextView>(R.id.address)
        val avatar = view.findViewById<ImageView>(R.id.avatar)
        val userName = view.findViewById<TextView>(R.id.user_name)
        val phoneNumber = view.findViewById<TextView>(R.id.phone_number)
        val postTime = view.findViewById<TextView>(R.id.post_time)
        val bedroom = view.findViewById<TextView>(R.id.bedroom)
        val bathroom = view.findViewById<TextView>(R.id.bathroom)

        // Gán dữ liệu vào view
        Glide.with(context).load(post.imageMain).into(mainImage)
        Glide.with(context).load(post.smallImage1).into(smallImage1)
        Glide.with(context).load(post.smallImage2).into(smallImage2)
        Glide.with(context).load(post.smallImage3).into(smallImage3)
        title.text = post.title
        price.text = formatPrice(post.price)
        area.text = post.area.toInt().toString() + " m²"
        address.text = post.location
        Glide.with(context).load(post.avatar).transform(CircleCrop()).into(avatar)
        userName.text = post.userName
        phoneNumber.text = post.phoneNumber
        postTime.text = post.postTime
        bedroom.text = post.bedrooms.toString()
        bathroom.text = post.bathrooms.toString()


        // Interactivity
        val favoriteButton = view.findViewById<ImageView>(R.id.favorite_button)
        var isFavorite = post.isFavorite
        favoriteButton.setImageResource(if (isFavorite) R.drawable.favorite_icon_2 else R.drawable.favorite_icon)

        favoriteButton.setOnClickListener {
            isFavorite = !isFavorite
            post.isFavorite = isFavorite
            favoriteButton.setImageResource(if (isFavorite) R.drawable.favorite_icon_2 else R.drawable.favorite_icon)
            if (isFavorite) {
                UserRepository.getInstance().addFavorite(post.id) { success ->
                    if (success) {
                        callback.onFavoriteButtonClicked(post.id)
                        Log.d("PostAdapter", "Add favorite success")
                    } else {
                        Log.d("PostAdapter", "Add favorite failed")
                    }
                }
            } else {
                UserRepository.getInstance().removeFavorite(post.id) { success ->
                    if (success) {
                        callback.onFavoriteButtonClicked(post.id)
                        Log.d("PostAdapter", "Remove favorite success")
                    } else {
                        Log.d("PostAdapter", "Remove favorite failed")
                    }
                }
            }
        }

        val callButton = view.findViewById<LinearLayout>(R.id.call_button)
        callButton.setOnClickListener {
            Log.d("PostAdapter", "Call button clicked")
            // Call the phone number
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:${post.phoneNumber}")
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.CALL_PHONE),
                    1
                )
            } else {
                context.startActivity(intent)
            }
        }

        return view
    }

    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}