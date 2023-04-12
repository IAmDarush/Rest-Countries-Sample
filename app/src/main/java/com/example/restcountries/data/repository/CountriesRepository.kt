package com.example.restcountries.data.repository

import androidx.paging.PagingData
import com.example.restcountries.data.remote.model.Country
import com.example.restcountries.ui.countries.CountriesFilter
import kotlinx.coroutines.flow.Flow

interface CountriesRepository {

    fun getEuropeanCountries(countriesFilter: CountriesFilter? = null): Flow<PagingData<Country>>

}