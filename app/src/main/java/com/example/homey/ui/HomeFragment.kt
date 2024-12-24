package com.example.homey

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.homey.adapters.PostAdapter
import com.example.homey.data.model.Estate
import com.example.homey.data.model.Post
import com.example.homey.data.repository.EstateRepository
import com.example.homey.ui.SearchActivity

class HomeFragment : Fragment() {
    private lateinit var adapter: PostAdapter
    private lateinit var searchBar: EditText
    private lateinit var noResultsText: TextView
    private val estateRepository = EstateRepository.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Hide action bar
        (activity as? AppCompatActivity)?.supportActionBar?.hide()

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


        val adapter = PostAdapter(requireContext(), posts)
        val listView = view.findViewById<ListView>(R.id.itemPost)
        searchBar = view.findViewById(R.id.searchBar)
        noResultsText = view.findViewById(R.id.noResultsText)

        listView.adapter = adapter

        searchBar.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivity(intent)
        }
        return view
    }




}