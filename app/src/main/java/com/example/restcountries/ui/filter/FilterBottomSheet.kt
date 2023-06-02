package com.example.restcountries.ui.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.OptIn
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.navGraphViewModels
import com.example.restcountries.R
import com.example.restcountries.data.model.SortType
import com.example.restcountries.data.model.Subregion
import com.example.restcountries.databinding.BottomSheetFilterBinding
import com.example.restcountries.ui.countries.CountriesViewModel
import com.example.restcountries.utils.ViewUtils
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class FilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFilterBinding? = null
    private val binding get() = _binding!!
    private val countriesViewModel: CountriesViewModel by navGraphViewModels(R.id.countriesNavGraph) {
        defaultViewModelProviderFactory
    }
    private val viewModel: FilterViewModel by viewModels()
    private var badgeDrawable: BadgeDrawable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFilterBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    @OptIn(ExperimentalBadgeUtils::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Prepare the filters badge once the views are laid out
        binding.btnResetAll.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            @OptIn(ExperimentalBadgeUtils::class)
            override fun onGlobalLayout() {
                badgeDrawable =
                    BadgeDrawable.createFromResource(requireContext(), R.xml.filter_badge).apply {
                        horizontalOffsetWithText = binding.frameLayout.width / 4
                        verticalOffsetWithText =
                            binding.frameLayout.height / 2 + ViewUtils.dpToPx(resources, 24f)
                                .toInt() / 2
                        BadgeUtils.attachBadgeDrawable(
                            this, binding.btnResetAll, binding.frameLayout
                        )
                    }
                updateBadgeDrawable(viewModel.uiState.value.filterCount)
                binding.btnResetAll.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        // Fixes bottom sheet not fully expanded inside its parent view
        requireDialog().setOnShowListener {
            val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
            bottomSheetBehavior.isHideable = false
            val bottomSheetParent = binding.bottomSheetParent
            BottomSheetBehavior.from(bottomSheetParent.parent as View).peekHeight =
                bottomSheetParent.height
            bottomSheetBehavior.peekHeight = bottomSheetParent.height
            bottomSheetParent.parent.requestLayout()
        }

        binding.cgSortTypes.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) viewModel.setSortType(SortType.NONE)
            else when (checkedIds.first()) {
                R.id.chipAlphabeticalSort -> viewModel.setSortType(SortType.ALPHABETICAL_ASC)
                R.id.chipByPopulationSort -> viewModel.setSortType(SortType.POPULATION_ASC)
            }
        }

        for (index in 0 until binding.cgSubregion.childCount) {
            val chip = binding.cgSubregion.getChildAt(index) as Chip
            val subregion = getSubregionFromString(chip.text.toString())
            requireNotNull(subregion) { "subregion must not be null" }
            chip.isChecked = viewModel.uiState.value.subregions.contains(subregion)
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.selectSubregion(subregion)
                else viewModel.deselectSubregion(subregion)
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.map { it.filterCount }.distinctUntilChanged()
                        .collect { count ->
                            updateBadgeDrawable(count)
                        }
                }

                launch {
                    viewModel.uiState.map { it.applyAllFilters }.distinctUntilChanged()
                        .collect { shouldApplyFilters ->
                            if (shouldApplyFilters) {
                                val filterData = FilterData(
                                    sortType = viewModel.uiState.value.sortType,
                                    subregions = viewModel.uiState.value.subregions
                                )
                                countriesViewModel.applyFilters(filterData)
                                dismiss()
                            }
                        }
                }

                launch {
                    viewModel.uiState.map { it.clearAllFilters }.distinctUntilChanged()
                        .collect { shouldResetFilters ->
                            if (shouldResetFilters) {
                                countriesViewModel.resetFilters()
                                dismiss()
                            }
                        }
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

    private fun getSubregionFromString(text: String): Subregion? {
        for (subregion in Subregion.values()) {
            if (subregion.subregion == text) {
                return subregion
            }
        }

        return null
    }

}