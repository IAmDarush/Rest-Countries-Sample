package com.example.restcountries.data.remote.model

import com.google.gson.annotations.SerializedName


data class Country(
    @SerializedName("name") var name: Name? = Name(),
    @SerializedName("capital") var capital: ArrayList<String> = arrayListOf(),
    @SerializedName("subregion") var subregion: String? = null,
    @SerializedName("languages") var languages: ArrayList<Map<String, String>> = arrayListOf(),
    @SerializedName("borders") var borders: ArrayList<String> = arrayListOf(),
    @SerializedName("area") var area: Int? = null,
    @SerializedName("flag") var flag: String? = null,
    @SerializedName("population") var population: Int? = null,
)

data class Name(
    @SerializedName("common") var common: String? = null,
    @SerializedName("official") var official: String? = null,
)