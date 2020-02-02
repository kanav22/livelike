package com.example.android.hiveproject

import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.indwealth.core.util.debouncedOnClick
import com.indwealth.core.util.inflate
import com.indwealth.core.util.shortRupeeString
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.restaurant_list_item.view.*
import kotlinx.android.synthetic.main.restaurants_title.view.*
import kotlinx.android.synthetic.main.search_bar.view.*


class RestaurantListAdapter(private val proposalSelected: SearchSelected) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val list = mutableListOf<RestaurantItem>()

    fun addItems(list: List<RestaurantItem>) {
        this.list.clear()
        this.list.addAll(list)
        notifyItemRangeInserted(1, list.size)
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = parent.inflate(viewType)
        return when (viewType) {
            R.layout.restaurants_title -> RestaurantTitleViewHolder(view)
            R.layout.restaurant_list_item -> RestaurantViewHolder(view)
            R.layout.search_bar -> SearchViewHolder(view)
            else -> RestaurantTitleViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            R.layout.restaurants_title -> (holder as RestaurantTitleViewHolder).bind(list[position] as RestaurantItem.Title)
            R.layout.restaurant_list_item -> (holder as RestaurantViewHolder).bind(list[position] as RestaurantItem.RestaurantList)
            R.layout.search_bar -> (holder as SearchViewHolder).bind(list[position] as RestaurantItem.SearchBar)
        }
    }


    inner class RestaurantTitleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleTv: TextView = view.findViewById(R.id.titleTv)


        fun bind(subtitle: RestaurantItem.Title) {
            titleTv.text = subtitle.title
            itemView.countText.text = subtitle.count.toString() + " results"
        }
    }

    inner class RestaurantViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(data: RestaurantItem.RestaurantList) {

            itemView.nameTextView.text = data.restaurantList.restaurant.name
            itemView.ratingTextView.text =
                "Rating: " + data.restaurantList.restaurant.user_rating.rating_text
            itemView.costTextView.text =
                "Cost for two: " + data.restaurantList.restaurant.average_cost_for_two.shortRupeeString()
            itemView.localityTextView.text =
                "Location: " + data.restaurantList.restaurant.location.city
            if (data.restaurantList.restaurant.featured_image.isNotEmpty()) {
                Picasso.get()
                    .load(data.restaurantList.restaurant.featured_image)
                    .into(itemView.thumbImageView)
            } else {
                if (data.restaurantList.restaurant.thumb.isNotEmpty()) {
                    Picasso.get()
                        .load(data.restaurantList.restaurant.thumb)
                        .into(itemView.thumbImageView)
                }

            }

        }
    }

    inner class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(data: RestaurantItem.SearchBar) {
            with(itemView) {

                searchButton.debouncedOnClick {
                    if (!edit_text.text.isNullOrEmpty()) {
                        proposalSelected.sendSearchText(edit_text.text.toString())
                    }
                }

                filterButton.debouncedOnClick {
                    PopupMenu(
                        this.context,
                        filterButton,
                        R.style.Theme_MaterialComponents_Light
                    ).apply {
                        menuInflater.inflate(R.menu.filter_options, menu)
                        setOnMenuItemClickListener {
                            when (it.itemId) {
                                R.id.moreOption -> {
                                    proposalSelected.sendSearchText(
                                        edit_text.text.toString(),
                                        "cost"
                                    )

                                }
                                R.id.moreOption2 -> {
                                    proposalSelected.sendSearchText(
                                        edit_text.text.toString(),
                                        "cost",
                                        10
                                    )

                                }
                            }
                            return@setOnMenuItemClickListener false
                        }
                    }.show()
                }

            }
        }
    }

    interface SearchSelected {
        fun sendSearchText(searchText: String, sort: String = "rating", count: Int = 15)
    }
}

