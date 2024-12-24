package com.example.homey.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.homey.R
import com.example.homey.data.model.Estate
import com.example.homey.ui.EditEstateActivity
import com.example.homey.utils.formatPrice
import kotlin.math.floor

class EstateAdapter(private var estates: List<Estate>, private val startForResult: ActivityResultLauncher<Intent>) : RecyclerView.Adapter<EstateAdapter.EstateViewHolder>() {

    class EstateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mainImage: ImageView = itemView.findViewById(R.id.mainImage)
        val smallImage1: ImageView = itemView.findViewById(R.id.smallImage1)
        val smallImage2: ImageView = itemView.findViewById(R.id.smallImage2)
        val smallImage3: ImageView = itemView.findViewById(R.id.smallImage3)
        val title: TextView = itemView.findViewById(R.id.title)
        val price: TextView = itemView.findViewById(R.id.price)
        val area: TextView = itemView.findViewById(R.id.area)
        val pricePerArea: TextView = itemView.findViewById(R.id.pricePerArea)
        val address: TextView = itemView.findViewById(R.id.address)
        val bedrooms: TextView = itemView.findViewById(R.id.bedroom)
        val bathrooms: TextView = itemView.findViewById(R.id.bathroom)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstateViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.my_estate_item, parent, false)
        return EstateViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: EstateViewHolder, position: Int) {
        val estate = estates[position]
        Glide.with(holder.itemView.context)
            .load(estate.images[0])
            .into(holder.mainImage)
        Glide.with(holder.itemView.context)
            .load(estate.images[1])
            .into(holder.smallImage1)
        Glide.with(holder.itemView.context)
            .load(estate.images[2])
            .into(holder.smallImage2)
        Glide.with(holder.itemView.context)
            .load(estate.images[3])
            .into(holder.smallImage3)
        holder.title.text = estate.title
        holder.price.text = formatPrice(estate.price)
        holder.area.text = estate.size.toInt().toString() + " m²"
        holder.pricePerArea.text = formatPrice(floor(estate.price / estate.size)) + "/m²"
        holder.address.text = estate.location
        holder.bedrooms.text = estate.bedrooms.toString()
        holder.bathrooms.text = estate.bathrooms.toString()


        holder.itemView.setOnClickListener {
            // Handle the click event
            val intent = Intent(holder.itemView.context, EditEstateActivity::class.java)
            intent.putExtra("estateId", estate.id)
            intent.putExtra("estateTitle", estate.title)
            intent.putExtra("estatePropertyType", estate.propertyType)
            intent.putExtra("estatePrice", estate.price)
            intent.putExtra("estateSize", estate.size)
            intent.putExtra("estateLocation", estate.location)
            intent.putExtra("estateBedrooms", estate.bedrooms)
            intent.putExtra("estateBathrooms", estate.bathrooms)
            intent.putStringArrayListExtra("estateImages", ArrayList(estate.images))
            startForResult.launch(intent)
        }
    }

    override fun getItemCount() = estates.size

    fun updateEstates(newEstates: List<Estate>) {
        estates = newEstates
        notifyDataSetChanged()
    }


}