package com.example.android.hiveproject

import androidx.annotation.LayoutRes
import model.Restaurant

sealed class RestaurantItem(@LayoutRes val viewType: Int) {
    data class Title(val title: String, val count: Int) : RestaurantItem(R.layout.restaurants_title)
    data class RestaurantList(val restaurantList: Restaurant) :
        RestaurantItem(R.layout.restaurant_list_item)

    data class SearchBar(val title: String) : RestaurantItem(R.layout.search_bar)
}