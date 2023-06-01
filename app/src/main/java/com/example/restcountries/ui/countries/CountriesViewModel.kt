package com.example.restcountries.ui.countries

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.restcountries.R
import com.example.restcountries.data.model.CountriesFilterModel
import com.example.restcountries.data.model.SortType
import com.example.restcountries.data.repository.CountriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class CountriesViewModel @Inject constructor(
    countriesRepository: CountriesRepository
) : ViewModel() {

    data class UiState(
        val filter: CountriesFilterModel = CountriesFilterModel(),
        val showErrorLayout: Boolean = false,
        @StringRes val errorMessageId: Int = R.string.message_error_unknown_error,
        val showCountriesList: Boolean = false,
        val isLoading: Boolean = false,
        val retryCount: Int = 0
    ) {

        val filterCount: Int
            get() {
                val sortCount = if (filter.sortType == SortType.NONE) 0 else 1
                val filterCount = filter.subregions.size
                return sortCount + filterCount
            }

    }

    data class FilterUiState(
        val sortType: SortType = SortType.NONE,
        val subregions: Set<String> = setOf()
    ) {
        val filterCount: Int
            get() {
                val sortCount = if (sortType == SortType.NONE) 0 else 1
                val filterCount = subregions.size
                return sortCount + filterCount
            }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _filterUiState = MutableStateFlow(FilterUiState())
    val filterUiState: StateFlow<FilterUiState> = _filterUiState.asStateFlow()

    private val filterFlow = uiState.map { it.filter }.distinctUntilChanged()
    private val retryFlow = uiState.map { it.retryCount }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    val countriesFlow =
        filterFlow.combine(retryFlow) { filter, _ -> filter }.flatMapLatest { filter ->
            countriesRepository.getEuropeanCountries(filter).map { pagingData ->
                pagingData.map { CountryItemUiState.mapDomainCountryToUi(it) }
            }
        }.cachedIn(viewModelScope)

    fun search(searchQuery: String) {
        _uiState.update {
            it.copy(filter = it.filter.copy(searchQuery = searchQuery))
        }
    }

    fun sortAlphabetically() {
        _filterUiState.update { it.copy(sortType = SortType.ALPHABETICAL_ASC) }
    }

    fun sortByPopulation() {
        _filterUiState.update { it.copy(sortType = SortType.POPULATION_ASC) }
    }

    fun selectSubregion(subregion: String) {
        val subregions = filterUiState.value.subregions.toMutableSet().apply {
            add(subregion)
        }
        _filterUiState.update { it.copy(subregions = subregions) }
    }

    fun deselectSubregion(subregion: String) {
        val subregions = filterUiState.value.subregions.toMutableSet().apply {
            remove(subregion)
        }
        _filterUiState.update { it.copy(subregions = subregions) }
    }

    fun applyFilters() {
        val filters = uiState.value.filter.copy(
            sortType = filterUiState.value.sortType,
            subregions = filterUiState.value.subregions
        )
        _uiState.update { it.copy(filter = filters) }
    }

    fun resetFilters() {
        _filterUiState.update { FilterUiState() }
        _uiState.update {
            it.copy(
                filter = it.filter.copy(
                    sortType = SortType.NONE,
                    subregions = setOf()
                )
            )
        }
    }

    fun sortByNone() {
        _filterUiState.update { it.copy(sortType = SortType.NONE) }
    }

    fun loadFailed(exception: Throwable?) {
        val errorMessageId = when (exception) {
            is UnknownHostException -> R.string.message_error_internet_error
            is HttpException -> R.string.message_error_server_error
            else -> R.string.message_error_unknown_error
        }
        _uiState.update {
            it.copy(
                showCountriesList = false,
                showErrorLayout = true,
                errorMessageId = errorMessageId,
                isLoading = false
            )
        }
    }

    fun loadSucceeded() {
        _uiState.update {
            it.copy(
                showCountriesList = true,
                showErrorLayout = false,
                isLoading = false
            )
        }
    }

    fun isLoading() {
        _uiState.update {
            it.copy(
                isLoading = true,
                showErrorLayout = false,
                showCountriesList = true
            )
        }
    }

    fun retryLoading() {
        _uiState.update {
            it.copy(
                isLoading = true,
                showErrorLayout = false,
                showCountriesList = true,
                retryCount = it.retryCount + 1
            )
        }
    }

}