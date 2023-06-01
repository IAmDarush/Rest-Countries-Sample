package com.example.restcountries.data.model

data class CountriesFilterModel(
    val searchQuery: String? = null,
    val sortType: SortType = SortType.NONE,
    val subregions: Set<String> = setOf(),
)

enum class SortType {
    NONE,
    ALPHABETICAL_ASC,
    POPULATION_ASC
}