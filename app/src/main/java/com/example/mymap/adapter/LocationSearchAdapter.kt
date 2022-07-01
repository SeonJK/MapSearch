package com.example.mymap.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mymap.databinding.ItemLocationBinding
import com.example.mymap.model.Place
import com.example.mymap.model.SearchResult

/**
 * MyMap
 * Created by SeonJK
 * Date: 2022-05-23
 * Time: 오후 5:46
 * */
class LocationSearchAdapter(
    private val itemClickedListener: (Place) -> Unit
) : ListAdapter<Place, LocationSearchAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(
        private val binding: ItemLocationBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(place: Place) {
            binding.buildingTextView.text = place.buildingName
            // 도로명 주소가 없을 때 동주소가 나올 수 있도록 설정
            binding.locationTextView.text =
                if(place.roadAddress != "") place.roadAddress else place.address

            binding.root.setOnClickListener {
                itemClickedListener(place)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        return ViewHolder(ItemLocationBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Place>() {
            override fun areItemsTheSame(
                oldItem: Place,
                newItem: Place,
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: Place,
                newItem: Place,
            ): Boolean {
                return oldItem.address == newItem.address
            }

        }

    }
}