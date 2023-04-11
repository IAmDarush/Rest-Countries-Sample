package com.example.restcountries.ui.countries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.navGraphViewModels
import com.example.restcountries.R
import com.example.restcountries.databinding.BottomSheetFilterBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip


class FilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFilterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CountriesViewModel by navGraphViewModels(R.id.countriesNavGraph) {
        defaultViewModelProviderFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}