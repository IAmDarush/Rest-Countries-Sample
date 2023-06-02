package com.example.restcountries.ui.filter

import androidx.lifecycle.SavedStateHandle
import com.example.restcountries.data.model.SortType
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
                subregions = setOf(Subregion.NORTHERN_EUROPE.subregion)
            )
            vm = FilterViewModel(savedStateHandle)

            vm.uiState.value.sortType shouldBe SortType.ALPHABETICAL_ASC
            vm.uiState.value.subregions.shouldContainAll("Northern Europe")
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

        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given the filter screen is opened, When the user wants to filter by a particular subregion, Then check all the selected subregions`() =
        runTest {

        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given the filter screen is opened, When the user wants deselect all the subregions, Then deselect the subregions`() =
        runTest {

        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given the filter screen is opened, When the user wants to clear all the filtering, Then clear all the filters`() =
        runTest {

        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given the filter screen is opened, When the user wants to apply all the filtering, Then apply the filters`() =
        runTest {

        }

}