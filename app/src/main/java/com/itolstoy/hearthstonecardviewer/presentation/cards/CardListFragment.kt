package com.itolstoy.hearthstonecardviewer.presentation.cards

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.itolstoy.hearthstonecardviewer.MainActivity
import com.itolstoy.hearthstonecardviewer.R
import com.itolstoy.hearthstonecardviewer.databinding.FragmentCardlistBinding
import com.itolstoy.hearthstonecardviewer.presentation.adapter.CardAdapter
import com.itolstoy.hearthstonecardviewer.presentation.details.CardFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CardListFragment : Fragment() {
    private var _binding: FragmentCardlistBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CardListViewModel by viewModels()
    lateinit var cardAdapter: CardAdapter
    private var currentAdapterPosition = 0
    private var tempAdapterPosition = 0
    var firstVisibleItemPosition = 0
    var topOffset = 0
    var tempFirstVisibleItemPosition = 0
    var tempTopOffset = 0
    private var flag = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCardlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cardAdapter = CardAdapter { position->
            val cardIds = cardAdapter.getCards().map { it.cardId }
            viewModel.saveCardIds(cardIds)
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.cardIdsSavedState.collect { isSaved ->
                    if (isSaved) {
                        val cardFragment = CardFragment().apply {
                            arguments = bundleOf(CardFragment.POSITION_ARG to position)
                        }
                            requireActivity().supportFragmentManager.beginTransaction()
                                .add(R.id.nav_host_fragment_activity_main, cardFragment)
                                .addToBackStack(null)
                                .commit()
                        (requireActivity() as MainActivity).binding.navView.visibility = View.GONE
                    }
                }
            }
        }

        val sortOptions = arrayOf(getString(R.string.sort_by_class), getString(R.string.sort_by_cost))
        val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sortOptions)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.sortSpinner.adapter = arrayAdapter

        val defaultPosition = 0
        binding.sortSpinner.setSelection(defaultPosition)

        viewModel.setSortOrder(
            if (binding.radioAscending.isChecked)
                SortOrder.ASCENDING
            else
                SortOrder.DESCENDING
        )

        binding.sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> viewModel.sortCardsByClass()
                    1 -> viewModel.sortCardsByCost()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.sortOrderGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioAscending -> viewModel.setSortOrder(SortOrder.ASCENDING)
                R.id.radioDescending -> viewModel.setSortOrder(SortOrder.DESCENDING)
            }

            when (binding.sortSpinner.selectedItemPosition) {
                0 -> viewModel.sortCardsByClass()
                1 -> viewModel.sortCardsByCost()
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.cardsFlow.collect { cards ->
                    cardAdapter.setCards(cards)
                }
            }
        }

        setFragmentResultListener("card_slider_changes_key") { key, bundle ->
            val changed = bundle.getBoolean("changed", false)
            if (changed) {
                if (viewModel.filteredCards.isNotEmpty()) {
                    viewModel.updateFilteredList()
                } else {
                    viewModel.getCards()
                }
            }
        }
        setFragmentResultListener("card_favourites_key") { key, bundle ->
            val changed = bundle.getBoolean("changed", false)
            if (changed) {
                viewModel.getCards()
            }
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = cardAdapter
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.stateFlow
                    .collect { uiState ->
                    when (uiState) {
                        is CardListFragmentState.OK -> {
                            binding.progressBar.visibility = View.GONE
                        }
                        is CardListFragmentState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is CardListFragmentState.Success -> {
                            cardAdapter.setCards(uiState.list)
                            binding.progressBar.visibility = View.GONE
                        }
                        is CardListFragmentState.AddedFromSearchList -> {
                            binding.progressBar.visibility = View.GONE
                            cardAdapter.setCards(uiState.list)
                        }
                        is CardListFragmentState.FilteredCards -> {
                            cardAdapter.setCards(uiState.list)
                            binding.recyclerView.scrollToPosition(0)
                        }
                        is CardListFragmentState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), uiState.message, Toast.LENGTH_LONG)
                        }
                    }
                }
            }
        }

        val layoutManager =
            binding.recyclerView.layoutManager as LinearLayoutManager

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    enableSortingControls()
                    firstVisibleItemPosition = tempFirstVisibleItemPosition
                    topOffset = tempTopOffset
                    flag = true
                    cardAdapter.setCards(viewModel.cards)
                    layoutManager.scrollToPositionWithOffset(firstVisibleItemPosition, topOffset)
                } else {
                    firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    topOffset = layoutManager.findViewByPosition(firstVisibleItemPosition)?.top ?: 0
                    if (flag) {
                        disableSortingControls()

                        tempAdapterPosition = currentAdapterPosition
                        tempFirstVisibleItemPosition = firstVisibleItemPosition
                        tempTopOffset = topOffset
                        flag = false
                    }
                    viewModel.searchCards(newText)
                }
                return true
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun disableSortingControls() {

        binding.sortSpinner.isEnabled = false
        binding.sortOrderGroup.isEnabled = false
        binding.radioAscending.isEnabled = false
        binding.radioDescending.isEnabled = false

        binding.sortSpinner.visibility = View.GONE
        binding.sortOrderGroup.visibility = View.GONE
        binding.radioAscending.visibility = View.GONE
        binding.radioDescending.visibility = View.GONE
    }

    private fun enableSortingControls() {
        binding.sortSpinner.isEnabled = true
        binding.sortOrderGroup.isEnabled = true
        binding.radioAscending.isEnabled = true
        binding.radioDescending.isEnabled = true

        binding.sortSpinner.visibility = View.VISIBLE
        binding.sortOrderGroup.visibility = View.VISIBLE
        binding.radioAscending.visibility = View.VISIBLE
        binding.radioDescending.visibility = View.VISIBLE
    }
}