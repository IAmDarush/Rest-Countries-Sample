package com.example.restcountries.ui.countries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.navigation.navGraphViewModels
import com.example.restcountries.R
import com.example.restcountries.databinding.BottomSheetFilterBinding
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import androidx.annotation.OptIn
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.restcountries.utils.ViewUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class FilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFilterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CountriesViewModel by navGraphViewModels(R.id.countriesNavGraph) {
        defaultViewModelProviderFactory
    }
    private var badgeDrawable: BadgeDrawable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFilterBinding.inflate(inflater, container, false)
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
                updateBadgeDrawable(viewModel.filterUiState.value.filterCount)
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

        binding.chipAlphabeticalSort.isChecked =
            (viewModel.filterUiState.value.sortType == SortType.ALPHABETICAL_ASC)

        binding.chipByPopulationSort.isChecked =
            (viewModel.filterUiState.value.sortType == SortType.POPULATION_ASC)

        binding.btnApply.setOnClickListener {
            viewModel.applyFilters()
            dismiss()
        }

        binding.btnResetAll.setOnClickListener {
            viewModel.resetFilters()
            dismiss()
        }

        binding.cgSortTypes.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) viewModel.sortByNone()
            else when (checkedIds.first()) {
                R.id.chipAlphabeticalSort -> viewModel.sortAlphabetically()
                R.id.chipByPopulationSort -> viewModel.sortByPopulation()
            }
        }

        for (index in 0 until binding.cgSubregion.childCount) {
            val chip = binding.cgSubregion.getChildAt(index) as Chip
            chip.isChecked = viewModel.filterUiState.value.subregions.contains(chip.text.toString())
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.selectSubregion(chip.text.toString())
                else viewModel.deselectSubregion(chip.text.toString())
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filterUiState.map { it.filterCount }.distinctUntilChanged()
                    .collect { count ->
                        updateBadgeDrawable(count)
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