package com.example.restcountries.data.repository

import androidx.paging.PagingData
import com.example.restcountries.data.remote.model.Country
import kotlinx.coroutines.flow.Flow

interface CountriesRepository {

    fun getEuropeanCountries(): Flow<PagingData<Country>>

}