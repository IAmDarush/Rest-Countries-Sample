package com.example.restcountries.data.repository

import androidx.paging.PagingData
import com.example.restcountries.data.remote.model.Country
import com.example.restcountries.ui.countries.CountriesFilterModel
import kotlinx.coroutines.flow.Flow

interface CountriesRepository {

    fun getEuropeanCountries(countriesFilterModel: CountriesFilterModel? = null): Flow<PagingData<Country>>

}