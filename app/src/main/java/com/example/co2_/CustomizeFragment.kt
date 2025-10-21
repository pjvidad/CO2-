package com.example.co2_

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment

class CustomizeFragment : Fragment() {
    private var selectedAccessoryResId: Int? = null
    private var lastSelectedImageView: ImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.shop_customize, container, false)
    }

    @SuppressLint("CommitTransaction", "ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val redeemButton = view.findViewById<Button>(R.id.button_redeem)

        redeemButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ShopFragment())
        }
        val accessory1 = view.findViewById<ImageView>(R.id.accessory_image_view_1)
        val accessory2 = view.findViewById<ImageView>(R.id.accessory_image_view_2)
        val accessory3 = view.findViewById<ImageView>(R.id.accessory_image_view_3)
        val accessory4 = view.findViewById<ImageView>(R.id.accessory_image_view_4)
        val accessory5 = view.findViewById<ImageView>(R.id.accessory_image_view_5)
        val accessory6 = view.findViewById<ImageView>(R.id.accessory_image_view_6)
        val accessoryImageViews = listOf(accessory1, accessory2, accessory3, accessory4, accessory5, accessory6)


        accessoryImageViews.forEach { imageView ->
            imageView.setOnClickListener { clickedView ->
                lastSelectedImageView?.setBackgroundResource(R.drawable.card_background)
                clickedView.setBackgroundResource(R.drawable.selected_card_background)
                selectedAccessoryResId = clickedView.tag as? Int
                lastSelectedImageView = clickedView as ImageView
            }
        }
    }
}