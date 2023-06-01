package com.example.restcountries.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.restcountries.data.remote.CountriesService
import com.example.restcountries.data.remote.model.Country
import com.example.restcountries.data.model.CountriesFilterModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CountriesRepositoryImpl @Inject constructor(
    private val countriesService: CountriesService
) : CountriesRepository {
    // TODO: cache the getEuropeanCountries network response here until the server is able
    //  to handle filtered queries

    override fun getEuropeanCountries(countriesFilterModel: CountriesFilterModel?): Flow<PagingData<Country>> {
        val config = PagingConfig(
            pageSize = NETWORK_PAGE_SIZE,
            enablePlaceholders = false,
            initialLoadSize = 10,
            prefetchDistance = 5
        )
        return Pager(
            config = config,
            pagingSourceFactory = { CountriesPagingSource(countriesService, countriesFilterModel) }
        ).flow
    }

}