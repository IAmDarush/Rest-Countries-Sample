package com.example.restcountries.ui.filter

import androidx.lifecycle.SavedStateHandle
import com.example.restcountries.data.model.SortType
import com.example.restcountries.data.model.Subregion
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FilterViewModelTest {

    private lateinit var vm: FilterViewModel
    private val savedStateHandle = SavedStateHandle()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given the filter screen is opened, When there is a saved instance state, Then update the ui state accordingly`() =
        runTest {
            savedStateHandle[KEY_FILTER_DATA] = FilterData(
                sortType = SortType.ALPHABETICAL_ASC,
                subregions = setOf(Subregion.NORTHERN_EUROPE)
            )
            vm = FilterViewModel(savedStateHandle)

            vm.uiState.value.sortType shouldBe SortType.ALPHABETICAL_ASC
            vm.uiState.value.subregions.shouldContainAll(Subregion.NORTHERN_EUROPE)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given the filter screen is opened, When the user wants to sort alphabetically, Then check the alphabetical sort order`() =
        runTest {
            vm = FilterViewModel(savedStateHandle)
            vm.uiState.value.sortType shouldBe SortType.NONE

            vm.setSortType(SortType.ALPHABETICAL_ASC)

            vm.uiState.value.sortType shouldBe SortType.ALPHABETICAL_ASC
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given the filter screen is opened, When the user wants deselect the sort type, Then select none of the sort types`() =
        runTest {
            vm = FilterViewModel(savedStateHandle)
            vm.setSortType(SortType.ALPHABETICAL_ASC)
            vm.uiState.value.sortType shouldBe SortType.ALPHABETICAL_ASC

            vm.setSortType(SortType.NONE)

            vm.uiState.value.sortType shouldBe SortType.NONE
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given the filter screen is opened, When the user wants to filter by a particular subregion, Then check all the selected subregions`() =
        runTest {
            vm = FilterViewModel(savedStateHandle)
            vm.uiState.value.subregions.shouldBeEmpty()

            vm.selectSubregion(Subregion.EASTERN_EUROPE)
            vm.selectSubregion(Subregion.NORTHERN_EUROPE)

            vm.uiState.value.subregions.shouldContainAll(
                Subregion.NORTHERN_EUROPE,
                Subregion.EASTERN_EUROPE
            )
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given the filter screen is opened, When the user wants deselect all the subregions, Then deselect the subregions`() =
        runTest {
            vm = FilterViewModel(savedStateHandle)
            vm.selectSubregion(Subregion.EASTERN_EUROPE)
            vm.selectSubregion(Subregion.NORTHERN_EUROPE)
            vm.uiState.value.subregions.size shouldBe 2

            vm.deselectSubregion(Subregion.EASTERN_EUROPE)
            vm.deselectSubregion(Subregion.NORTHERN_EUROPE)

            vm.uiState.value.subregions.shouldBeEmpty()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given the filter screen is opened, When the user wants to clear all the filtering, Then clear all the filters`() =
        runTest {
            vm = FilterViewModel(savedStateHandle)
            vm.setSortType(SortType.POPULATION_ASC)
            vm.selectSubregion(Subregion.SOUTHEAST_EUROPE)
            vm.selectSubregion(Subregion.NORTHERN_EUROPE)
            vm.selectSubregion(Subregion.WESTERN_EUROPE)
            vm.uiState.value.filterCount shouldBe 4
            vm.uiState.value.clearAllFilters shouldBe false

            vm.resetFilters()

            vm.uiState.value.filterCount shouldBe 0
            vm.uiState.value.sortType shouldBe SortType.NONE
            vm.uiState.value.subregions.shouldBeEmpty()
            vm.uiState.value.clearAllFilters shouldBe true
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given the filter screen is opened, When the user wants to apply all the filtering, Then apply the filters`() =
        runTest {
            vm = FilterViewModel(savedStateHandle)
            vm.setSortType(SortType.POPULATION_ASC)
            vm.selectSubregion(Subregion.SOUTHEAST_EUROPE)
            vm.selectSubregion(Subregion.NORTHERN_EUROPE)
            vm.uiState.value.filterCount shouldBe 3
            vm.uiState.value.applyAllFilters shouldBe false

            vm.applyFilters()

            vm.uiState.value.applyAllFilters shouldBe true
        }

}