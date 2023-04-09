package com.example.restcountries.data.remote

import com.example.restcountries.data.remote.model.Country
import retrofit2.http.GET

interface CountriesService {

    @GET("region/europe")
    suspend fun getEuropeanCountries(): List<Country>

}