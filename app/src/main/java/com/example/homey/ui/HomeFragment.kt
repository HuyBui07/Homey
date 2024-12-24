package com.example.homey

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.homey.adapters.PostAdapter
import com.example.homey.data.model.Post
import com.example.homey.ui.DetailEstateActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
class HomeFragment : Fragment() {
    private lateinit var listView: ListView
    private lateinit var adapter: PostAdapter
    private lateinit var posts: List<Post>
    private var selectedType: String = "Tất cả"
    private var selectedPrice: String = "Tất cả"
    private var selectedSort = "Tin mới nhất"

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

        // Generate data
        posts = listOf(
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
            )
        )

        listView = view.findViewById(R.id.itemPost)
        adapter = PostAdapter(requireContext(), posts)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedPost = posts[position]

            val intent = Intent(requireContext(), DetailEstateActivity::class.java)
            intent.putExtra("selectedPost", selectedPost)
            startActivity(intent)
        }

        val filterTypeButton = view.findViewById<Button>(R.id.filterType)
        filterTypeButton.setOnClickListener {
            showFilterTypeDialog()
        }

        val filterPriceButton = view.findViewById<Button>(R.id.filterPrice)
        filterPriceButton.setOnClickListener {
            showFilterPriceDialog()
        }

        val filterSortButton = view.findViewById<Button>(R.id.filterSort)
        filterSortButton.setOnClickListener {
            showSortDialog()
        }

        return view
    }

    private fun showFilterTypeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_filter, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogView)


        val listName = dialogView.findViewById<TextView>(R.id.filterTitle)
        listName.setText("Chọn loại nhà")

        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnCloseFilter)
        val listFilterTypes = dialogView.findViewById<ListView>(R.id.listFilterTypes)

        val filterTypes = listOf("Tất cả nhà đất", "Căn hộ chung cư", "Nhà bán", "Đất bán", "Khác")
        val filterAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, filterTypes)
        listFilterTypes.adapter = filterAdapter

        listFilterTypes.setOnItemClickListener { parent, view, position, id ->
            selectedType = filterTypes[position]
            applyFiltersAndSorting()
            dialog.dismiss()
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.show()
    }

    private fun showFilterPriceDialog() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_filter, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogView)

        val listName = dialogView.findViewById<TextView>(R.id.filterTitle)
        listName.setText("Chọn giá nhà")

        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnCloseFilter)
        val listFilterPrices = dialogView.findViewById<ListView>(R.id.listFilterTypes)

        val filterPrices = listOf("Tất cả mức giá", "Dưới 500 triệu", "500-800 triệu", "800 triệu - 1 tỷ", "Trên 1 tỷ")
        val filterAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, filterPrices)
        listFilterPrices.adapter = filterAdapter

        listFilterPrices.setOnItemClickListener { parent, view, position, id ->
            selectedPrice = filterPrices[position]
            applyFiltersAndSorting()
            dialog.dismiss()
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.show()
    }

    private fun showSortDialog() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_filter, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogView)

        val listName = dialogView.findViewById<TextView>(R.id.filterTitle)
        listName.setText("Sắp xếp theo")

        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnCloseFilter)
        val listSortOptions = dialogView.findViewById<ListView>(R.id.listFilterTypes)

        val sortOptions = listOf("Thông thường", "Giá giảm", "Giá tăng", "Diện tích giảm", "Diện tích tăng")

        val sortAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, sortOptions)
        listSortOptions.adapter = sortAdapter

        listSortOptions.setOnItemClickListener { _, _, position, _ ->
            selectedSort = sortOptions[position]
            applyFiltersAndSorting()
            dialog.dismiss()
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.show()
    }

    private fun applyFiltersAndSorting() {
        val filteredPosts = posts.filter {
            val matchesType = when (selectedType) {
                "Căn hộ chung cư" -> it.type == "apartment"
                "Nhà bán" -> it.type == "house"
                "Đất bán" -> it.type == "land"
                "Khác" -> it.type == "other"
                else -> true
            }
            val matchesPrice = when (selectedPrice) {
                "Dưới 500 triệu" -> it.price < 500000000
                "500-800 triệu" -> it.price >= 500000000 && it.price <800000000
                "800 triệu - 1 tỷ" -> it.price >= 800000000 && it.price < 1000000000
                "Trên 1 tỷ" -> it.price >= 1000000000
                else -> true
            }
            matchesType && matchesPrice
        }

        val sortedPosts = when (selectedSort) {
            "Giá tăng" -> filteredPosts.sortedBy { it.price }
            "Giá giảm" -> filteredPosts.sortedByDescending { it.price }
            "Diện tích tăng" -> filteredPosts.sortedBy { it.area }
            "Diện tích giảm" -> filteredPosts.sortedByDescending { it.area }
            else -> filteredPosts
        }


        adapter = PostAdapter(requireContext(), filteredPosts)
        listView.adapter = adapter
    }
}

