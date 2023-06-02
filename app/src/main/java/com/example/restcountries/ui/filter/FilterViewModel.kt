package com.example.restcountries.ui.filter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.restcountries.data.model.SortType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

const val KEY_FILTER_DATA = "filter_data"

data class FilterData(
    val sortType: SortType = SortType.NONE,
    val subregions: Set<String> = setOf()
) : java.io.Serializable

enum class Subregion(val subregion: String) {
    NORTHERN_EUROPE("Northern Europe"),
    WESTERN_EUROPE("Western Europe"),
    SOUTHERN_EUROPE("Southern Europe"),
    SOUTHEAST_EUROPE("Southeast Europe"),
    CENTRAL_EUROPE("Central Europe"),
    EASTERN_EUROPE("Eastern Europe")
}

class FilterViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    data class UiState(
        val sortType: SortType = SortType.NONE,
        val subregions: Set<String> = setOf()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {

        savedStateHandle.get<FilterData>(KEY_FILTER_DATA)?.let { filterData ->
            _uiState.update {
                it.copy(sortType = filterData.sortType, subregions = filterData.subregions)
            }
        }

    }

    fun setSortType(sortType: SortType) {
        _uiState.update {
            it.copy(sortType = sortType)
        }
    }

}