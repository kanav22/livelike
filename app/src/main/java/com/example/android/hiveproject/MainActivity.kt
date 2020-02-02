package com.example.android.hiveproject

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.indwealth.core.ui.ViewState
import data.FeatureBaseActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import model.SearchResponse
import viewmodel.ZomatoViewModel
import viewmodel.ZomatoViewModelFactory

class MainActivity : FeatureBaseActivity(), RestaurantListAdapter.SearchSelected {
    override fun sendSearchText(searchText: String, cost: String, count: Int) {
        // call zomato's api with the search string through viewModel
        viewModel.getRestaurantsData(searchText, cost, count)

    }

    override fun initViewModel() {
    }

    private lateinit var viewModel: ZomatoViewModel
    private lateinit var adapter: RestaurantListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // set clear toolbar for viewing icons
        setClearToolbar()

        viewModel = ViewModelProviders.of(
            this,
            ZomatoViewModelFactory(this.applicationContext as Application)
        ).get(ZomatoViewModel::class.java)
        lifecycleScope.launch {
            // get list of restaurants initially for displaying purpose
            viewModel.getRestaurantsData()
        }
        // observe livedata and set UI according to response
        viewModel.liveDataRestaurantsList.observe(this, androidx.lifecycle
            .Observer<ViewState<SearchResponse>> {
                when (it) {
                    is ViewState.Loading -> showProgress()
                    is ViewState.Error -> {
                        hideProgress()
                    }
                    is ViewState.Data -> {
                        showNotEmptyState(it.data)
                        hideProgress()

                    }
                }
            })
    }


    private fun showNotEmptyState(res: SearchResponse) {
        adapter = RestaurantListAdapter(this)
        listRecyclerView.layoutManager = LinearLayoutManager(this)
        listRecyclerView.adapter = adapter
        // add custom list items according to views
        val list = mutableListOf<RestaurantItem>()
        list.add(RestaurantItem.SearchBar("Restaurant"))
        list.add(RestaurantItem.Title("All Restaurants", res.results_shown))

        // iterate through list for viewing list
        res.restaurants.forEach {
            list.add(RestaurantItem.RestaurantList(it))
        }
        adapter.addItems(list)
        adapter.notifyDataSetChanged()

    }

    private fun setClearToolbar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.statusBarColor = resources.getColor(R.color.white)
    }
}
