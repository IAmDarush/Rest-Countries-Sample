package com.example.restcountries.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.restcountries.data.remote.CountriesService
import com.example.restcountries.data.remote.model.Country
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CountriesRepositoryImpl @Inject constructor(
    private val countriesService: CountriesService
) : CountriesRepository {

    override fun getEuropeanCountries(searchQuery: String?): Flow<PagingData<Country>> {
        val config = PagingConfig(
            pageSize = NETWORK_PAGE_SIZE,
            enablePlaceholders = false,
            initialLoadSize = 10,
        )
        return Pager(
            config = config,
            pagingSourceFactory = { CountriesPagingSource(countriesService, searchQuery) }
        ).flow
    }

}