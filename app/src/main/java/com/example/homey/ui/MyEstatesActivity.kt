package com.example.homey.ui

import android.annotation.SuppressLint
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
    @SuppressLint("StringFormatMatches")
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
                "apartment",
                R.drawable.small_image_1,
                R.drawable.small_image_1,
                R.drawable.small_image_2,
                R.drawable.small_image_3,
                "Với 3 tỷ 8 sở hữu ngay căn hộ SAM Towers Đà Nẵng...",
                "Với 3 tỷ 8, bạn đã có thể sở hữu ngay căn hộ cao cấp tại SAM Towers Đà Nẵng – nơi hội tụ đầy đủ tiện ích hiện đại và không gian sống đẳng cấp. Đặc biệt, chủ đầu tư cam kết lợi nhuận lên đến 200 triệu đồng/năm, mang đến cơ hội đầu tư sinh lời hấp dẫn. Hỗ trợ vay vốn linh hoạt, lãi suất ưu đãi cùng các chính sách thanh toán thuận lợi. Đừng bỏ lỡ cơ hội sở hữu một căn hộ tại thành phố biển Đà Nẵng với giá trị gia tăng không ngừng!",
                3800000000,
                53,
                "71.7 triệu/m²",
                2,
                2,
                5,
                "Đông Nam",
                "Sổ đỏ/ Sổ hồng",
                "NT cơ bản",
                "Quận 7, Hồ Chí Minh",
                "Trương Đăng Nghĩa",
                "Đăng hôm nay",
                "037455****"
            ),
            Post(
                "vinhome",
                R.drawable.small_image_1,
                R.drawable.small_image_1,
                R.drawable.small_image_2,
                R.drawable.small_image_3,
                "Biệt thự Vinhomes Riverside",
                "Sở hữu ngay biệt thự đẳng cấp tại Vinhomes Riverside, khu đô thị xanh giữa lòng Hà Nội. Với thiết kế tinh tế, không gian rộng rãi, và hệ thống tiện ích 5 sao, đây là lựa chọn hoàn hảo cho những ai tìm kiếm một cuộc sống sang trọng và gần gũi với thiên nhiên. Cơ hội đầu tư hấp dẫn với giá trị gia tăng vượt trội trong tương lai. Hãy liên hệ ngay để sở hữu biệt thự Vinhomes Riverside – nơi khẳng định đẳng cấp sống thượng lưu!",
                15000000000,
                250,
                "71.7 triệu/m²",
                3,
                4,
                3,
                "Bắc",
                "Sổ đỏ/ Sổ hồng",
                "Không",
                "Quận 2, Hồ Chí Minh",
                "Nguyễn Văn A",
                "Đăng hôm qua",
                "098765****"
        ))

        val container = findViewById<LinearLayout>(R.id.container)
        val inflater = LayoutInflater.from(this)

        for (post in posts) {
            val itemView = inflater.inflate(R.layout.my_estate_item, container, false)
            val mainImage = itemView.findViewById<ImageView>(R.id.mainImage)
            val smallImage1 = itemView.findViewById<ImageView>(R.id.smallImage1)
            val smallImage2 = itemView.findViewById<ImageView>(R.id.smallImage2)
            val smallImage3 = itemView.findViewById<ImageView>(R.id.smallImage3)
            val title = itemView.findViewById<TextView>(R.id.detail_title2)
            val price = itemView.findViewById<TextView>(R.id.detail_price)
            val area = itemView.findViewById<TextView>(R.id.detail_area)
            val address = itemView.findViewById<TextView>(R.id.address)

            // Set data to views
            mainImage.setImageResource(post.imageMain)
            smallImage1.setImageResource(post.smallImage1)
            smallImage2.setImageResource(post.smallImage2)
            smallImage3.setImageResource(post.smallImage3)
            title.text = post.title
            area.text = getString(R.string.area_format, post.area)
            address.text = post.location



            container.addView(itemView)
        }


    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}