package com.example.homey.adapters

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.homey.R
import com.example.homey.data.model.Post

class PostAdapter(private val context: Context, private val posts: List<Post>) : BaseAdapter() {

    override fun getCount(): Int = posts.size

    override fun getItem(position: Int): Any = posts[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_mainpage, parent, false)
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
        val userName = view.findViewById<TextView>(R.id.user_name)
        val postTime = view.findViewById<TextView>(R.id.post_time)
        val phoneNumber = view.findViewById<TextView>(R.id.phone_number)

        // Gán dữ liệu vào view
        mainImage.setImageResource(post.imageMain)
        smallImage1.setImageResource(post.smallImage1)
        smallImage2.setImageResource(post.smallImage2)
        smallImage3.setImageResource(post.smallImage3)
        title.text = post.title
        price.text = post.price
        area.text = post.area
        address.text = post.location
        userName.text = post.userName
        postTime.text = post.postTime
        phoneNumber.text = post.phoneNumber

        // Interactivity
        val favoriteButton = view.findViewById<ImageView>(R.id.favorite_button)
        var isFavorite = post.isFavorite
        favoriteButton.setImageResource(if (isFavorite) R.drawable.favorite_icon_2 else R.drawable.favorite_icon)

        favoriteButton.setOnClickListener {
            isFavorite = !isFavorite
            post.isFavorite = isFavorite
            favoriteButton.setImageResource(if (isFavorite) R.drawable.favorite_icon_2 else R.drawable.favorite_icon)
        }


        return view
    }
}