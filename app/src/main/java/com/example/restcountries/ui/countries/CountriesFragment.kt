package com.example.restcountries.ui.countries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.restcountries.databinding.FragmentCountriesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@AndroidEntryPoint
class CountriesFragment : Fragment() {

    private val viewModel: CountriesViewModel by viewModels()
    private var _binding: FragmentCountriesBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var countriesAdapter: CountriesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCountriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(FlowPreview::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        countriesAdapter = CountriesAdapter()

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = countriesAdapter
            addItemDecoration(
                DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            )
        }

        binding.etSearch.onTextChanged().debounce(300).onEach {
            viewModel.search(it?.toString() ?: "")
        }.launchIn(lifecycleScope)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.countriesFlow.collectLatest {
                    countriesAdapter.submitData(it)
                }
            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

fun EditText.onTextChanged(): Flow<CharSequence?> {
    return callbackFlow {
        val listener = doOnTextChanged { text, _, _, _ -> trySend(text) }
        awaitClose { removeTextChangedListener(listener) }
    }.onStart {
        emit(text)
    }
}