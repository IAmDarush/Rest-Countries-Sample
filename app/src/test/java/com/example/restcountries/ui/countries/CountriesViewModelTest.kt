package com.example.restcountries.ui.countries

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.testing.asPagingSourceFactory
import androidx.paging.testing.asSnapshot
import com.example.restcountries.MainCoroutineRule
import com.example.restcountries.data.remote.model.Country
import com.example.restcountries.data.repository.CountriesRepository
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
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
            Country(),
            Country()
        )
    }

    private fun getCountriesListFlow(
        coroutineScope: CoroutineScope,
        deferred: CompletableDeferred<Unit>? = null
    ): Flow<PagingData<Country>> {
        val itemsFlow = flow {
            deferred?.await()
            emit(getDummyCountriesList())
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
            every { getEuropeanCountries() } returns getCountriesListFlow(this@runTest, deferred)
        }

        vm = CountriesViewModel(mockCountriesRepository)
        val countriesList = vm.countriesFlow.asSnapshot(this) {}

        countriesList.shouldBeEmpty()
        verify(exactly = 1) {
            mockCountriesRepository.getEuropeanCountries()
        }
        deferred.complete(Unit)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given the countries list is being fetched, When it it succeeds, Then populate the list`() =
        runTest {
            val deferred = CompletableDeferred<Unit>()
            mockCountriesRepository.apply {
                every { getEuropeanCountries() } returns getCountriesListFlow(
                    this@runTest, deferred
                )
            }

            vm = CountriesViewModel(mockCountriesRepository)
            var countriesList = vm.countriesFlow.asSnapshot(this) {}
            countriesList.shouldBeEmpty()

            deferred.complete(Unit)
            countriesList = vm.countriesFlow.asSnapshot(this) {}

            countriesList.shouldNotBeEmpty()
            verify(exactly = 1) {
                mockCountriesRepository.getEuropeanCountries()
            }
        }

    @Test
    fun `Given the countries list is being fetched, When it fails, Then display an error`() {

    }

    @Test
    fun `Given the countries list is fetched, When the user wants to search for a specific country, Then search for the country`() {

    }

    @Test
    fun `Given a specific country is being searched, When the result is found, Then reduce the list`() {

    }

    @Test
    fun `Given a specific country is being searched, When the result is not found, Then show empty list`() {

    }

    @Test
    fun `Given a specific country is being searched, When the search fails, Then show error message`() {

    }

}