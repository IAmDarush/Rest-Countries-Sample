package com.example.restcountries.ui.countries

import com.example.restcountries.data.remote.model.Country
import java.io.Serializable

data class CountryItemUiState(
    val name: String? = null,
    val capital: String? = null,
    val subregion: String? = null,
    val languages: List<String> = listOf(),
    val borders: List<String> = listOf(),
    val area: Float? = null,
    val flagUrl: String? = null,
    val population: Int? = null,
) : Serializable {

    val languagesCommaSeparated: String = languages.joinToString()
    val bordersCommaSeparated: String = borders.joinToString()

    companion object {
        fun mapDomainCountryToUi(country: Country): CountryItemUiState {
            return CountryItemUiState(
                name = country.name?.common,
                capital = if (country.capital.isNotEmpty()) country.capital[0] else null,
                subregion = country.subregion,
                languages = country.languages.values.toList(),
                borders = country.borders.toList(),
                area = country.area,
                flagUrl = country.flags?.pngImage,
                population = country.population
            )
        }
    }
}