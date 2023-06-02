package com.example.restcountries.ui.filter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.restcountries.data.model.SortType
import com.example.restcountries.data.model.Subregion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

const val KEY_FILTER_DATA = "filter_data"

data class FilterData(
    val sortType: SortType = SortType.NONE,
    val subregions: Set<Subregion> = setOf()
) : java.io.Serializable

class FilterViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    data class UiState(
        val sortType: SortType = SortType.NONE,
        val subregions: Set<Subregion> = setOf(),
        val clearAllFilters: Boolean = false,
        val applyAllFilters: Boolean = false
    ) {
        val filterCount: Int
            get() {
                val sortCount = if (sortType == SortType.NONE) 0 else 1
                val filterCount = subregions.size
                return sortCount + filterCount
            }
    }

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

    fun selectSubregion(subregion: Subregion) {
        _uiState.update {
            val subregions = it.subregions.toMutableSet().apply {
                add(subregion)
            }
            it.copy(subregions = subregions)
        }
    }

    fun deselectSubregion(subregion: Subregion) {
        _uiState.update {
            val subregions = it.subregions.toMutableSet().apply {
                remove(subregion)
            }
            it.copy(subregions = subregions)
        }
    }

    fun resetFilters() {
        _uiState.update {
            it.copy(clearAllFilters = true, subregions = setOf(), sortType = SortType.NONE)
        }
    }

    fun applyFilters() {
        _uiState.update {
            it.copy(applyAllFilters = true)
        }
    }

}