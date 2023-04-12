package com.example.restcountries.ui.countries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewTreeObserver
import android.widget.EditText
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.restcountries.R
import com.example.restcountries.databinding.FragmentCountriesBinding
import com.example.restcountries.utils.ViewUtils
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.google.android.material.color.MaterialColors
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

    private val viewModel by navGraphViewModels<CountriesViewModel>(R.id.countriesNavGraph) {
        defaultViewModelProviderFactory
    }
    private var _binding: FragmentCountriesBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var countriesAdapter: CountriesAdapter
    private var badgeDrawable: BadgeDrawable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCountriesBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    @OptIn(FlowPreview::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Prepare the filters badge once the views are laid out
        binding.btnFilter.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            @androidx.annotation.OptIn(ExperimentalBadgeUtils::class)
            override fun onGlobalLayout() {
                badgeDrawable =
                    BadgeDrawable.createFromResource(requireContext(), R.xml.filter_badge).apply {
                        horizontalOffsetWithText = ViewUtils.dpToPx(resources, 20f).toInt()
                        verticalOffsetWithText = ViewUtils.dpToPx(resources, 20f).toInt()
                        BadgeUtils.attachBadgeDrawable(this, binding.btnFilter)
                    }
                updateBadgeDrawable(viewModel.filterUiState.value.filterCount)
                binding.btnFilter.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { rv, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply the insets as a margin to the view. Here the system is setting
            // only the bottom, left, and right dimensions, but apply whichever insets are
            // appropriate to your layout. You can also update the view padding
            // if that's more appropriate.
            val mlp = rv.layoutParams as MarginLayoutParams
            mlp.leftMargin = insets.left
            mlp.bottomMargin = insets.bottom
            mlp.rightMargin = insets.right
            rv.layoutParams = mlp

            // Return CONSUMED if you don't want want the window insets to keep being
            // passed down to descendant views.
            WindowInsetsCompat.CONSUMED
        }

        countriesAdapter = CountriesAdapter { item, navigatorExtras ->
            findNavController().navigate(
                CountriesFragmentDirections.actionCountriesFragmentToDetailsFragment(item),
                navigatorExtras = navigatorExtras
            )
        }

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

        binding.btnFilter.setOnClickListener {
            findNavController().navigate(
                CountriesFragmentDirections.actionCountriesFragmentToFilterBottomSheet()
            )
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.countriesFlow.collectLatest {
                        countriesAdapter.submitData(it)
                    }
                }

                launch {
                    viewModel.uiState.map { it.filterCount }.distinctUntilChanged()
                        .collect { count -> updateBadgeDrawable(count) }
                }
            }
        }

        // RecyclerView must wait for any data to load and for the RecyclerView items to be
        // ready to draw before starting the transition animation.
        postponeEnterTransition()

        viewLifecycleOwner.lifecycleScope.launch {
            countriesAdapter.loadStateFlow.collectLatest { loadStates ->
                when (val loadState = loadStates.refresh) {
                    is LoadState.NotLoading -> {
                        viewModel.loadSucceeded()
                        (view.parent as? ViewGroup)?.doOnPreDraw {
                            // RecyclerView items are ready. Begin postponed transitions.
                            startPostponedEnterTransition()
                        }
                    }
                    LoadState.Loading -> viewModel.isLoading()
                    is LoadState.Error -> viewModel.loadFailed(loadState.error)
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateBadgeDrawable(number: Int) {
        when (number > 0) {
            true -> {
                badgeDrawable?.number = number
                badgeDrawable?.backgroundColor =
                    MaterialColors.getColor(
                        requireView(),
                        com.google.android.material.R.attr.colorError
                    )
            }
            false -> {
                badgeDrawable?.clearNumber()
                badgeDrawable?.backgroundColor =
                    ResourcesCompat.getColor(resources, android.R.color.transparent, null)
            }
        }
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