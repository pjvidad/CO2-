package com.example.co2_

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.co2_.databinding.CarouselItemsBinding

class CarouselAdapter(private val carouselData: List<Pair<Int, String>>, private val onClick: (String) -> Unit) :
    RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {

    inner class CarouselViewHolder(private val binding: CarouselItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(imageRes: Int, url: String) {
            binding.carouselImageView.setImageResource(imageRes)
            binding.carouselImageView.setOnClickListener {
                onClick(url)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val binding =
            CarouselItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarouselViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        val (imageRes, url) = carouselData[position]
        holder.bind(imageRes, url)
    }

    override fun getItemCount(): Int {
        return carouselData.size
    }
}