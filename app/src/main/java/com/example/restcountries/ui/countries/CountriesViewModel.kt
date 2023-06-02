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
import com.example.restcountries.ui.filter.FilterData
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

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

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

    fun applyFilters(filters: FilterData) {
        _uiState.update {
            it.copy(
                filter = it.filter.copy(
                    sortType = filters.sortType,
                    subregions = filters.subregions
                )
            )
        }
    }

    fun resetFilters() {
        _uiState.update {
            it.copy(
                filter = it.filter.copy(
                    sortType = SortType.NONE,
                    subregions = setOf()
                )
            )
        }
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