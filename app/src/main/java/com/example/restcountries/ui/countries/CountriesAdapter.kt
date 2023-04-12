package com.example.restcountries.ui.countries

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.restcountries.databinding.ItemCountryBinding
import com.example.restcountries.ui.details.DetailsFragment

class CountriesAdapter(
    val onItemClick: (item: CountryItemUiState, navigatorExtras: FragmentNavigator.Extras) -> Unit
) : PagingDataAdapter<CountryItemUiState, CountriesAdapter.CountryItemViewHolder>(COUNTRY_DIFFER) {

    override fun onBindViewHolder(holder: CountryItemViewHolder, position: Int) {
        holder.bindData(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCountryBinding.inflate(inflater, parent, false)
        return CountryItemViewHolder(binding)
    }

    inner class CountryItemViewHolder(private val binding: ItemCountryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private fun clearData() {
            binding.tvCountry.text = ""
            binding.tvCapital.text = ""
            binding.tvSubRegion.text = ""
        }

        fun bindData(position: Int) {
            clearData()

            getItem(position)?.let { item ->
                ViewCompat.setTransitionName(binding.tvCountry, "transition_country_$position")

                binding.tvCountry.text = item.name
                binding.tvSubRegion.text = item.subregion
                binding.tvCapital.text = item.capital

                binding.root.setOnClickListener {
                    val navigatorExtras = FragmentNavigatorExtras(
                        binding.tvCountry to DetailsFragment.ViewTransitions.COUNTRY_NAME_TRANSITION
                    )
                    onItemClick(item, navigatorExtras)
                }
            }

        }

    }
}

val COUNTRY_DIFFER = object : DiffUtil.ItemCallback<CountryItemUiState>() {
    override fun areItemsTheSame(oldItem: CountryItemUiState, newItem: CountryItemUiState) =
        oldItem.name == newItem.name

    override fun areContentsTheSame(
        oldItem: CountryItemUiState,
        newItem: CountryItemUiState
    ): Boolean {
        return oldItem.name == newItem.name &&
                oldItem.capital == newItem.capital &&
                oldItem.subregion == newItem.subregion
    }
}