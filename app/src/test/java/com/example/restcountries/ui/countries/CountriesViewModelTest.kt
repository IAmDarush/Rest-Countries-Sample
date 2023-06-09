package com.example.restcountries.ui.countries

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.testing.asPagingSourceFactory
import androidx.paging.testing.asSnapshot
import com.example.restcountries.MainCoroutineRule
import com.example.restcountries.R
import com.example.restcountries.data.model.CountriesFilterModel
import com.example.restcountries.data.model.SortType
import com.example.restcountries.data.model.Subregion
import com.example.restcountries.data.remote.model.Country
import com.example.restcountries.data.remote.model.Name
import com.example.restcountries.data.repository.CountriesRepository
import com.example.restcountries.ui.filter.FilterData
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.net.UnknownHostException
import java.util.*

class CountriesViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK(relaxed = true)
    lateinit var mockCountriesRepository: CountriesRepository

    private lateinit var vm: CountriesViewModel

    private fun getDummyCountriesList(): List<Country> {
        return listOf(
            Country(name = Name(common = "Germany")),
            Country(name = Name(common = "France")),
            Country(name = Name(common = "England")),
            Country(name = Name(common = "Netherlands"))
        )
    }

    private fun getCountriesListFlow(
        coroutineScope: CoroutineScope,
        deferred: CompletableDeferred<Unit>? = null,
        searchQuery: String? = null
    ): Flow<PagingData<Country>> {
        val countriesList = getDummyCountriesList().filter {
            return@filter if (searchQuery != null)
                it.name?.common?.contains(searchQuery, ignoreCase = true) == true
            else true
        }
        val itemsFlow = flow {
            deferred?.await()
            emit(countriesList)
        }
        val pagingSourceFactory = itemsFlow.asPagingSourceFactory(coroutineScope = coroutineScope)
        val pagingConfig = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false
        )
        return Pager(config = pagingConfig) { pagingSourceFactory() }.flow
    }

    @Before
    fun setUp() {

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given the screen is opened, Then start fetching the countries list`() = runTest {
        val deferred = CompletableDeferred<Unit>()
        mockCountriesRepository.apply {
            every { getEuropeanCountries(CountriesFilterModel()) } returns
                    getCountriesListFlow(this@runTest, deferred)
        }

        vm = CountriesViewModel(mockCountriesRepository)
        vm.uiState.value.isLoading shouldBe false
        vm.uiState.value.showErrorLayout shouldBe false
        vm.uiState.value.showCountriesList shouldBe false
        val countriesList = vm.countriesFlow.asSnapshot(this) {}
        vm.isLoading()

        vm.uiState.value.isLoading shouldBe true
        vm.uiState.value.showErrorLayout shouldBe false
        vm.uiState.value.showCountriesList shouldBe true
        countriesList.shouldBeEmpty()
        verify(exactly = 1) {
            mockCountriesRepository.getEuropeanCountries(CountriesFilterModel())
        }
        deferred.complete(Unit)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given the countries list is being fetched, When it succeeds, Then populate the list`() =
        runTest {
            val deferred = CompletableDeferred<Unit>()
            mockCountriesRepository.apply {
                every { getEuropeanCountries(CountriesFilterModel()) } returns getCountriesListFlow(
                    this@runTest, deferred
                )
            }

            vm = CountriesViewModel(mockCountriesRepository)
            vm.uiState.value.isLoading shouldBe false
            vm.uiState.value.showCountriesList shouldBe false
            vm.uiState.value.showErrorLayout shouldBe false
            var countriesList = vm.countriesFlow.asSnapshot(this) {}
            countriesList.shouldBeEmpty()

            deferred.complete(Unit)
            countriesList = vm.countriesFlow.asSnapshot(this) {}
            vm.loadSucceeded()

            countriesList.shouldNotBeEmpty()
            vm.uiState.value.isLoading shouldBe false
            vm.uiState.value.showCountriesList shouldBe true
            vm.uiState.value.showErrorLayout shouldBe false
            verify(exactly = 1) {
                mockCountriesRepository.getEuropeanCountries(CountriesFilterModel())
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given the countries list is being fetched, When it fails, Then display the error`() =
        runTest {
            vm = CountriesViewModel(mockCountriesRepository)
            vm.uiState.value.showErrorLayout shouldBe false
            vm.uiState.value.showCountriesList shouldBe false

            vm.loadFailed(UnknownHostException())

            vm.uiState.value.showErrorLayout shouldBe true
            vm.uiState.value.errorMessageId shouldBe R.string.message_error_internet_error
            vm.uiState.value.showCountriesList shouldBe false
            vm.uiState.value.isLoading shouldBe false
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given the network error is displayed, When the user wants to retry, Then start loading the data again`() =
        runTest {
            mockCountriesRepository.apply {
                every { getEuropeanCountries(any()) } returns getCountriesListFlow(this@runTest)
            }
            vm = CountriesViewModel(mockCountriesRepository)
            vm.countriesFlow.asSnapshot(this) {}

            vm.loadFailed(null)
            vm.uiState.value.isLoading shouldBe false
            vm.uiState.value.showErrorLayout shouldBe true
            vm.uiState.value.showCountriesList shouldBe false

            vm.retryLoading()
            vm.uiState.value.isLoading shouldBe true
            vm.uiState.value.showErrorLayout shouldBe false
            vm.uiState.value.showCountriesList shouldBe true
            verify(exactly = 2) {
                mockCountriesRepository.getEuropeanCountries(any())
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given the countries list is fetched, When the user wants to search for a specific country, Then reduce the list`() =
        runTest {
            val dummySearchQuery = "germany"
            mockCountriesRepository.apply {
                every {
                    getEuropeanCountries(CountriesFilterModel())
                } returns getCountriesListFlow(this@runTest)
                every { getEuropeanCountries(CountriesFilterModel(dummySearchQuery)) } returns getCountriesListFlow(
                    this@runTest, searchQuery = dummySearchQuery
                )
            }

            vm = CountriesViewModel(mockCountriesRepository)
            var countriesList = vm.countriesFlow.asSnapshot(this) {}
            countriesList.size shouldBe 4

            vm.search(dummySearchQuery)

            countriesList = vm.countriesFlow.asSnapshot(this) {}
            countriesList.size shouldBe 1
            vm.uiState.value.filter.searchQuery shouldBe dummySearchQuery
            verify(exactly = 1) {
                mockCountriesRepository.getEuropeanCountries(CountriesFilterModel())
                mockCountriesRepository.getEuropeanCountries(CountriesFilterModel("germany"))
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given a specific country is being searched, When the result is not found, Then show empty list`() =
        runTest {
            val dummySearchQuery = "invalidCountry"
            mockCountriesRepository.apply {
                every { getEuropeanCountries(CountriesFilterModel()) } returns getCountriesListFlow(
                    this@runTest
                )
                every { getEuropeanCountries(CountriesFilterModel(dummySearchQuery)) } returns getCountriesListFlow(
                    this@runTest, searchQuery = dummySearchQuery
                )
            }

            vm = CountriesViewModel(mockCountriesRepository)
            var countriesList = vm.countriesFlow.asSnapshot(this) {}
            countriesList.size shouldBe 4

            vm.search(dummySearchQuery)

            countriesList = vm.countriesFlow.asSnapshot(this) {}
            countriesList.size shouldBe 0
            vm.uiState.value.filter.searchQuery shouldBe dummySearchQuery
            verify(exactly = 1) {
                mockCountriesRepository.getEuropeanCountries(CountriesFilterModel())
                mockCountriesRepository.getEuropeanCountries(CountriesFilterModel(dummySearchQuery))
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given the countries list is ready, When the user wants to sort alphabetically, Then sort the list by alphabetic order ascending`() =
        runTest {
            mockCountriesRepository.apply {
                every {
                    getEuropeanCountries(CountriesFilterModel())
                } returns getCountriesListFlow(this@runTest)
            }
            vm = CountriesViewModel(mockCountriesRepository)
            val countriesList = vm.countriesFlow.asSnapshot(this) {}
            countriesList.size shouldBe 4
            vm.uiState.value.filter.sortType shouldBe SortType.NONE
            vm.uiState.value.filterCount shouldBe 0

            val filterData = FilterData(sortType = SortType.ALPHABETICAL_ASC)
            vm.applyFilters(filterData)

            vm.uiState.value.filter.sortType shouldBe SortType.ALPHABETICAL_ASC
            vm.uiState.value.filterCount shouldBe 1
            verify {
                mockCountriesRepository.getEuropeanCountries(
                    CountriesFilterModel(sortType = SortType.ALPHABETICAL_ASC)
                )
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given the countries list is ready, When the user wants to sort by population, Then sort the list by population ascending`() =
        runTest {
            mockCountriesRepository.apply {
                every {
                    getEuropeanCountries(CountriesFilterModel())
                } returns getCountriesListFlow(this@runTest)
            }
            vm = CountriesViewModel(mockCountriesRepository)
            val countriesList = vm.countriesFlow.asSnapshot(this) {}
            countriesList.size shouldBe 4
            vm.uiState.value.filter.sortType shouldBe SortType.NONE
            vm.uiState.value.filterCount shouldBe 0

            val filterData = FilterData(sortType = SortType.POPULATION_ASC)
            vm.applyFilters(filterData)

            vm.uiState.value.filter.sortType shouldBe SortType.POPULATION_ASC
            vm.uiState.value.filterCount shouldBe 1
            verify(exactly = 1) {
                mockCountriesRepository.getEuropeanCountries(CountriesFilterModel())
                mockCountriesRepository.getEuropeanCountries(
                    CountriesFilterModel(sortType = SortType.POPULATION_ASC)
                )
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given the countries list is ready, When the user wants to filter by a particular subregion, Then show filtered list`() =
        runTest {
            val emptyFilter = CountriesFilterModel()
            mockCountriesRepository.apply {
                every { getEuropeanCountries(emptyFilter) } returns getCountriesListFlow(this@runTest)
            }
            vm = CountriesViewModel(mockCountriesRepository)
            val countriesList = vm.countriesFlow.asSnapshot(this) {}
            countriesList.size shouldBe 4
            vm.uiState.value.filter.subregions shouldBe setOf()
            vm.uiState.value.filterCount shouldBe 0

            val subregion = Subregion.WESTERN_EUROPE
            val filterData = FilterData(subregions = setOf(subregion))
            vm.applyFilters(filterData)

            vm.uiState.value.filter.subregions.shouldContainOnly(subregion)
            vm.uiState.value.filterCount shouldBe 1
            verify(exactly = 1) {
                mockCountriesRepository.getEuropeanCountries(emptyFilter)
                mockCountriesRepository.getEuropeanCountries(
                    CountriesFilterModel(subregions = setOf(subregion))
                )
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given the countries list is ready, When the user wants to clear all the filtering, Then show the whole list`() =
        runTest {
            val dummySearchQuery = "dummySearch"
            val subregions = setOf(Subregion.NORTHERN_EUROPE, Subregion.WESTERN_EUROPE)
            val filterData = FilterData(
                sortType = SortType.ALPHABETICAL_ASC,
                subregions = subregions
            )
            val emptyFilter = CountriesFilterModel(
                searchQuery = null,
                sortType = SortType.NONE,
                subregions = setOf()
            )
            val fullFilter = CountriesFilterModel(
                searchQuery = dummySearchQuery,
                sortType = filterData.sortType,
                subregions = filterData.subregions
            )
            val searchFilter = CountriesFilterModel(searchQuery = dummySearchQuery)
            mockCountriesRepository.apply {
                every { getEuropeanCountries(emptyFilter) } returns getCountriesListFlow(this@runTest)
                every { getEuropeanCountries(fullFilter) } returns getCountriesListFlow(this@runTest)
            }
            vm = CountriesViewModel(mockCountriesRepository)
            val countriesList = vm.countriesFlow.asSnapshot(this) {}
            countriesList.size shouldBe 4
            vm.search(dummySearchQuery)
            vm.applyFilters(filterData)
            vm.uiState.value.filter.sortType shouldBe SortType.ALPHABETICAL_ASC
            vm.uiState.value.filter.subregions shouldBe subregions
            vm.uiState.value.filterCount shouldBe 3

            vm.resetFilters()

            vm.uiState.value.filterCount shouldBe 0
            vm.uiState.value.filter.sortType shouldBe SortType.NONE
            vm.uiState.value.filter.subregions shouldBe setOf()
            vm.uiState.value.filter.searchQuery shouldBe dummySearchQuery
            verify(exactly = 1) {
                mockCountriesRepository.getEuropeanCountries(fullFilter)
                mockCountriesRepository.getEuropeanCountries(emptyFilter)
            }
            verify(exactly = 2) {
                mockCountriesRepository.getEuropeanCountries(searchFilter)
            }
        }

}