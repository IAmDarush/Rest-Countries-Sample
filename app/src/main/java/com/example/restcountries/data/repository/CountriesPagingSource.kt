package com.example.restcountries.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.restcountries.data.remote.CountriesService
import com.example.restcountries.data.remote.model.Country
import retrofit2.HttpException
import java.io.IOException

private const val STARTING_PAGE_INDEX = 0
const val NETWORK_PAGE_SIZE = 10

class CountriesPagingSource(
    private val countriesService: CountriesService,
    private val searchQuery: String?
) : PagingSource<Int, Country>() {

    // FIXME: remove local caching once the server is able to send paginated data
    private var cachedCountries = listOf<Country>()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Country> {
        val pageIndexKey = params.key ?: STARTING_PAGE_INDEX
        return try {
            if (cachedCountries.isEmpty()) {
                cachedCountries = countriesService.getEuropeanCountries().filter {
                    if (searchQuery != null)
                        it.name?.common?.contains(searchQuery, ignoreCase = true) == true
                    else true
                }
            }

            val countries = cachedCountries.subList(pageIndexKey * params.loadSize, params.loadSize)

            val nextKey =
                if (countries.isEmpty()) {
                    null
                } else {
                    // By default, initial load size = 3 * NETWORK PAGE SIZE
                    // ensure we're not requesting duplicating items at the 2nd request
                    pageIndexKey + (params.loadSize / NETWORK_PAGE_SIZE)
                }
            val prevKey = if (pageIndexKey == STARTING_PAGE_INDEX) null else pageIndexKey

            LoadResult.Page(data = countries, prevKey = prevKey, nextKey = nextKey)
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Country>): Int? {
        // We need to get the previous key (or next key if previous is null) of the page
        // that was closest to the most recently accessed index.
        // Anchor position is the most recently accessed index.
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }


}