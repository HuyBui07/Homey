package com.example.homey.ui

import ImageSliderAdapter
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.homey.R
import com.example.homey.data.model.Post
import com.example.homey.data.repository.EstateRepository
import com.example.homey.data.repository.UserRepository

class DetailEstateActivity : AppCompatActivity() {
    private lateinit var estateRepo: EstateRepository
    private lateinit var userRepository: UserRepository

    private lateinit var imageSlider: ViewPager2
    private lateinit var detail_price: TextView
    private lateinit var detail_area: TextView
    private lateinit var detail_pricePerArea: TextView
    private lateinit var detail_bedroom: TextView
    private lateinit var detail_bathroom: TextView
    private lateinit var title1: TextView
    private lateinit var detail_map: TextView
    private lateinit var description: TextView
    private lateinit var desc_price: TextView
    private lateinit var desc_area: TextView
    private lateinit var desc_frontage: TextView
    private lateinit var desc_direction: TextView
    private lateinit var desc_legal: TextView
    private lateinit var desc_furnitures: TextView
    private lateinit var username: TextView
    private lateinit var phoneNumber: TextView

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

        // Khởi tạo các biến
        estateRepo = EstateRepository.getInstance()
        userRepository = UserRepository.getInstance()

        // Lấy dữ liệu từ Intent
        val postId = intent.getStringExtra("selectedPostId")

        // Tham chiếu đến các view
        imageSlider = findViewById<ViewPager2>(R.id.image_slider)
        detail_price = findViewById(R.id.detail_price)
        detail_area = findViewById(R.id.detail_area)
        detail_pricePerArea= findViewById(R.id.detail_pricePerArea)
        detail_bedroom= findViewById(R.id.detail_bedroom)
        detail_bathroom= findViewById(R.id.detail_bathroom)
        title1= findViewById(R.id.detail_title)
        detail_map= findViewById(R.id.detail_map)
        description= findViewById(R.id.description)

        desc_price= findViewById(R.id.desc_price)
        desc_area= findViewById(R.id.desc_area)
        desc_frontage= findViewById(R.id.desc_frontage)
        desc_direction= findViewById(R.id.desc_direction)
        desc_legal= findViewById(R.id.desc_legal)
        desc_furnitures= findViewById(R.id.desc_furnitures)

        username= findViewById(R.id.detail_userName)
        phoneNumber= findViewById(R.id.call_button)

        EstateRepository.getInstance().getEstateById(postId!!) { estate ->
            if (estate != null) {
                userRepository.getAvatarAndUsernameAndPhoneNumberAndFavoriteState(
                    estate.ownerUid,
                    estate.id!!
                ) { success, avatar, username, phoneNumber, isFavorite ->
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
                       displayPost(post)
                    }

                }
            }
        }
    }

    private fun displayPost(post: Post) {
        // Hiển thị thông tin bài đăng

            val imageUrls = listOf(
                post.imageMain,
                post.smallImage1,
                post.smallImage2,
                post.smallImage3
            )
            val adapter = ImageSliderAdapter(imageUrls)
            imageSlider.adapter = adapter

            detail_area.text = post.area.toString() + " m²"
            detail_bedroom.text = post.bedrooms.toString()
            detail_bathroom.text = post.bathrooms.toString()
            title1.text = post.title
            desc_area.text = post.area.toString() + " m²"

            description.text = post.description
            desc_frontage.text = post.frontage.toString()
            desc_direction.text = post.orientation
            desc_legal.text = post.legalStatus
            desc_furnitures.text = post.furnishings

            username.text = post.userName
            phoneNumber.text = post.phoneNumber

            if (post.price > 1000000000) {
                if ((post.price % 1000000000).toLong() == 0L) {
                    detail_price.text = (post.price/1000000000).toInt().toString() + " tỷ"
                    desc_price.text = (post.price/1000000000).toInt().toString() + " tỷ"
                } else {
                    detail_price.text = String.format("%.1f tỷ", post.price.toFloat() / 1000000000)
                    desc_price.text = String.format("%.1f tỷ", post.price.toFloat() / 1000000000)
                }
            } else {
                if ((post.price % 1000000) == 0.0) {
                    detail_price.text = (post.price/1000000).toInt().toString() + " triệu"
                    desc_price.text = (post.price/1000000).toInt().toString() + " triệu"
                } else {
                    detail_price.text = String.format("%.1f triệu", post.price.toFloat() / 1000000)
                    desc_price.text = String.format("%.1f triệu", post.price.toFloat() / 1000000)
                }
            }


            if (post.area.toInt() != 0) {
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