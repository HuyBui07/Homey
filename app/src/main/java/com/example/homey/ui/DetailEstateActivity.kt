package com.example.homey.ui

import ImageSliderAdapter
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.homey.R
import com.example.homey.data.model.Post

class DetailEstateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_estates)
        // Set the title of the action bar
        supportActionBar?.hide()

        val buttonBack = findViewById<ImageButton>(R.id.button_back)
        val buttonFavorite = findViewById<ImageButton>(R.id.button_favorite)
        buttonBack.setOnClickListener {
            finish()
        }

        // Lấy dữ liệu từ Intent
        val post: Post? = intent.getParcelableExtra("selectedPost", Post::class.java)

        // Tham chiếu đến các view
        val imageSlider = findViewById<ViewPager2>(R.id.image_slider)
        val detail_price: TextView = findViewById(R.id.detail_price)
        val detail_area: TextView = findViewById(R.id.detail_area)
        val detail_pricePerArea: TextView = findViewById(R.id.detail_pricePerArea)
        val detail_bedroom: TextView = findViewById(R.id.detail_bedroom)
        val detail_bathroom: TextView = findViewById(R.id.detail_bathroom)
        val title1: TextView = findViewById(R.id.detail_title)
        val detail_map: TextView = findViewById(R.id.detail_map)
        val description: TextView = findViewById(R.id.description)

        val desc_price: TextView = findViewById(R.id.desc_price)
        val desc_area: TextView = findViewById(R.id.desc_area)
        val desc_frontage: TextView = findViewById(R.id.desc_frontage)
        val desc_direction: TextView = findViewById(R.id.desc_direction)
        val desc_legal: TextView = findViewById(R.id.desc_legal)
        val desc_furnitures: TextView = findViewById(R.id.desc_furnitures)

        val username : TextView = findViewById(R.id.detail_userName)
        val phoneNumber : TextView = findViewById(R.id.call_button)

        // Hiển thị thông tin bài đăng
        if (post != null) {
            val imageUrls = listOf(
                post.imageMain,
                post.smallImage1,
                post.smallImage2,
                post.smallImage3
            )
            val adapter = ImageSliderAdapter(imageUrls)
            imageSlider.adapter = adapter

            detail_area.text = getString(R.string.area_format, post.area)
            detail_bedroom.text = post.beds.toString()
            detail_bathroom.text = post.baths.toString()
            title1.text = post.title
            description.text = post.desc

            desc_area.text = post.area.toString() + " m²"
            desc_frontage.text = post.frontage.toString() + " m"
            desc_direction.text = post.direction
            desc_legal.text = post.legal
            desc_furnitures.text = post.furniture

            username.text = post.userName
            phoneNumber.text = post.phoneNumber

            if (post.price > 1000000000) {
                if (post.price % 1000000000 == 0L) {
                    detail_price.text = (post.price/1000000000).toInt().toString() + " tỷ"
                    desc_price.text = (post.price/1000000000).toInt().toString() + " tỷ"
                } else {
                    detail_price.text = String.format("%.1f tỷ", post.price.toFloat() / 1000000000)
                    desc_price.text = String.format("%.1f tỷ", post.price.toFloat() / 1000000000)
                }
            } else {
                if (post.price % 1000000 == 0L) {
                    detail_price.text = (post.price/1000000).toInt().toString() + " triệu"
                    desc_price.text = (post.price/1000000).toInt().toString() + " triệu"
                } else {
                    detail_price.text = String.format("%.1f triệu", post.price.toFloat() / 1000000)
                    desc_price.text = String.format("%.1f triệu", post.price.toFloat() / 1000000)
                }
            }


            if (post.area != 0) {
                val ppa = post.price.toDouble() / post.area.toDouble() /1000000
                if(ppa >1000){
                    if (ppa % 1000 == 0.toDouble()) {
                        detail_pricePerArea.text = (ppa/1000).toInt().toString() + " tỷ/m²"
                    }
                    else {
                        detail_pricePerArea.text = String.format("%.1d tỷ/m²", ppa/1000)
                    }
                } else {
                    if (ppa % 1 == 0.toDouble()) {
                        detail_pricePerArea.text = ppa.toInt().toString() + " triệu/m²"
                    }
                    else {
                        detail_pricePerArea.text = String.format("%.1f triệu/m²", ppa)
                    }
                }

            }
        }
    }
}