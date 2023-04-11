package com.example.restcountries.ui.countries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.restcountries.data.remote.model.Country
import com.example.restcountries.data.repository.CountriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.io.Serializable
import javax.inject.Inject

data class CountryItemUiState(
    val name: String? = null,
    val capital: String? = null,
    val subregion: String? = null,
    val languages: List<String> = listOf(),
    val borders: List<String> = listOf(),
    val area: Float? = null,
    val flagUrl: String? = null,
    val population: Int? = null,
): Serializable {

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

data class CountriesFilter(
    val searchQuery: String? = null,
    val shouldSortAlphabetically: Boolean = false,
)

@HiltViewModel
class CountriesViewModel @Inject constructor(
    countriesRepository: CountriesRepository
) : ViewModel() {

    data class UiState(
        val filter: CountriesFilter = CountriesFilter()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val countriesFlow: Flow<PagingData<CountryItemUiState>> = uiState.flatMapLatest {
        flowOf(it.filter)
    }.distinctUntilChanged()
        .flatMapLatest { filter ->
            countriesRepository.getEuropeanCountries(filter).map { pagingData ->
                pagingData.map { CountryItemUiState.mapDomainCountryToUi(it) }
            }
        }.cachedIn(viewModelScope)

    fun search(searchQuery: String) {
        _uiState.update {
            it.copy(filter = it.filter.copy(searchQuery = searchQuery))
        }
    }

    fun sortAlphabetically(shouldSort: Boolean) {
        _uiState.update {
            it.copy(filter = it.filter.copy(shouldSortAlphabetically = shouldSort))
        }
    }

}