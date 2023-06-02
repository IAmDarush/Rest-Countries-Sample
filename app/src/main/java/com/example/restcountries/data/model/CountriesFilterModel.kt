package com.example.restcountries.data.model

data class CountriesFilterModel(
    val searchQuery: String? = null,
    val sortType: SortType = SortType.NONE,
    val subregions: Set<Subregion> = setOf(),
)

enum class SortType {
    NONE,
    ALPHABETICAL_ASC,
    POPULATION_ASC
}

enum class Subregion(val subregion: String) {
    NORTHERN_EUROPE("Northern Europe"),
    WESTERN_EUROPE("Western Europe"),
    SOUTHERN_EUROPE("Southern Europe"),
    SOUTHEAST_EUROPE("Southeast Europe"),
    CENTRAL_EUROPE("Central Europe"),
    EASTERN_EUROPE("Eastern Europe")
}