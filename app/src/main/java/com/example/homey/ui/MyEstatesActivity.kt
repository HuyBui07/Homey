package com.example.homey.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.homey.R
import com.example.homey.data.model.Post
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MyEstatesActivity : AppCompatActivity() {
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

        // Interactivity
        val addEstateButton = findViewById<FloatingActionButton>(R.id.fab)
        addEstateButton.setOnClickListener {
            val intent = Intent(this, AddRealEstateActivity::class.java)
            startActivity(intent)
        }

        val posts = listOf(
            Post(
                R.drawable.small_image_1,
                R.drawable.small_image_1,
                R.drawable.small_image_2,
                R.drawable.small_image_3,
                "Với 3 tỷ 8 sở hữu ngay căn hộ SAM Towers Đà Nẵng, cam kết lợi nhuận 200tr/năm, hỗ trợ... ",
                "3.8 tỷ",
                "53 m²",
                "71.7 triệu/m²",
                "Quận 7, Hồ Chí Minh",
                "Trương Đăng Nghĩa",
                "Đăng hôm nay",
                "037455****"
            ),
            Post(
                R.drawable.small_image_1,
                R.drawable.small_image_1,
                R.drawable.small_image_2,
                R.drawable.small_image_3,
                "Biệt thự Vinhomes Riverside",
                "15 tỷ",
                "250 m²",
                "71.7 triệu/m²",
                "Quận 2, Hồ Chí Minh",
                "Nguyễn Văn A",
                "Đăng hôm qua",
                "098765****"
            )
        )

        val container = findViewById<LinearLayout>(R.id.container)
        val inflater = LayoutInflater.from(this)

        for (post in posts) {
            val itemView = inflater.inflate(R.layout.my_estate_item, container, false)

            val mainImage = itemView.findViewById<ImageView>(R.id.mainImage)
            val smallImage1 = itemView.findViewById<ImageView>(R.id.smallImage1)
            val smallImage2 = itemView.findViewById<ImageView>(R.id.smallImage2)
            val smallImage3 = itemView.findViewById<ImageView>(R.id.smallImage3)
            val title = itemView.findViewById<TextView>(R.id.title)
            val price = itemView.findViewById<TextView>(R.id.price)
            val area = itemView.findViewById<TextView>(R.id.area)
            val address = itemView.findViewById<TextView>(R.id.address)

            // Set data to views
            mainImage.setImageResource(post.imageMain)
            smallImage1.setImageResource(post.smallImage1)
            smallImage2.setImageResource(post.smallImage2)
            smallImage3.setImageResource(post.smallImage3)
            title.text = post.title
            price.text = post.price
            area.text = post.area
            address.text = post.location

            container.addView(itemView)
        }


    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}