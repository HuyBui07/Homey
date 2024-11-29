package com.example.homey


import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

public data class Post(
    val imageMain: Int,
    val smallImage1: Int,
    val smallImage2: Int,
    val smallImage3: Int,
    val title: String,
    val price: String,
    val area: String,
    val pricePerArea: String,
    val location: String,
    val userName: String,
    val postTime: String,
    val phoneNumber: String
)

class MainPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Hide action bar
        supportActionBar?.hide()

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

        val listView = findViewById<ListView>(R.id.itemPost)
        val adapter = PostAdapter(this, posts)

        listView.adapter = adapter

    }
}