package com.example.homey.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        val title = view.findViewById<TextView>(R.id.detail_title2)
        val price = view.findViewById<TextView>(R.id.detail_price)
        val area = view.findViewById<TextView>(R.id.detail_area)
        val pricePerArea = view.findViewById<TextView>(R.id.detail_pricePerArea)
        val bedroom = view.findViewById<TextView>(R.id.detail_bedroom)
        val bathroom = view.findViewById<TextView>(R.id.detail_bathroom)
        val address = view.findViewById<TextView>(R.id.address)
        val userName = view.findViewById<TextView>(R.id.user_name)
        val postTime = view.findViewById<TextView>(R.id.post_time)
        val phoneNumber = view.findViewById<TextView>(R.id.phone_number)

        // Gán dữ liệu vào view
        area.text = post.area.toString() + " m²"
        mainImage.setImageResource(post.imageMain)
        smallImage1.setImageResource(post.smallImage1)
        smallImage2.setImageResource(post.smallImage2)
        smallImage3.setImageResource(post.smallImage3)
        title.text = post.title
        bedroom.text = post.beds.toString()
        bathroom.text = post.baths.toString()
        address.text = post.location
        userName.text = post.userName
        postTime.text = post.postTime
        phoneNumber.text = post.phoneNumber

        if (post.price > 1000000000) {
            if (post.price % 1000000000 == 0L) {
                price.text = (post.price/1000000000).toInt().toString() + " tỷ"
            } else {
                price.text = String.format("%.1f tỷ", post.price.toFloat() / 1000000000)
            }
        } else {
            if (post.price % 1000000 == 0L) {
                price.text = (post.price/1000000).toInt().toString() + " triệu"
            } else {
                price.text = String.format("%.1f triệu", post.price.toFloat() / 1000000)
            }
        }


        if (post.area != 0) {
            val ppa = post.price.toDouble() / post.area.toDouble() /1000000
            if(ppa >1000){
                if (ppa % 1000 == 0.toDouble()) {
                    pricePerArea.text = (ppa/1000).toInt().toString() + " tỷ/m²"
                }
                else {
                    pricePerArea.text = String.format("%.1d tỷ/m²", ppa/1000)
                }
            } else {
                if (ppa % 1 == 0.toDouble()) {
                    pricePerArea.text = ppa.toInt().toString() + " triệu/m²"
                }
                else {
                    pricePerArea.text = String.format("%.1f triệu/m²", ppa)
                }
            }

        }

        return view
    }
}